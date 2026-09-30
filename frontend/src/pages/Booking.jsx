import { useEffect, useMemo, useState } from 'react';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import TripBookingPanel from '../components/TripBookingPanel';
import BookingCheckout from '../components/BookingCheckout';
import BookingConfirmation from '../components/BookingConfirmation';
import BookingStepper from '../components/BookingStepper';
import { useApi } from '../hooks/useApi';
import { useLivePrices } from '../hooks/useLivePrices';
import { api } from '../services/api';
import { clampNights, createDefaultStaySchedule, addDaysToDate } from '../utils/booking';
import { useLocation } from 'react-router-dom';
import './Booking.css';

export default function Booking() {
  const location = useLocation();
  const { data, loading, error, refetch } = useApi(() => api.getBooking(), []);
  const { data: hotels } = useApi(() => api.getHotels(), []);

  const [selectedTripId, setSelectedTripId] = useState(null);
  const [step, setStep] = useState('grid');
  const [schedule, setSchedule] = useState(() => createDefaultStaySchedule());
  const [selectedHotelId, setSelectedHotelId] = useState(null);
  const [confirmation, setConfirmation] = useState(null);

  const selectedTrip = useMemo(
    () => data?.options.find((option) => option.id === selectedTripId) ?? null,
    [data, selectedTripId]
  );

  const selectedHotel = useMemo(
    () => hotels?.find((hotel) => hotel.id === selectedHotelId) ?? null,
    [hotels, selectedHotelId]
  );

  const { prices } = useLivePrices(
    selectedTrip?.relatedHotelIds,
    schedule.nights,
    step === 'checkout' && Boolean(selectedTrip)
  );

  const selectedLivePrice = prices.find((price) => price.hotelId === selectedHotelId);

  useEffect(() => {
    const preferredHotelId = location.state?.preferredHotelId;
    const preferredSchedule = location.state?.staySchedule;

    if (!preferredHotelId || !data?.options?.length) {
      return;
    }

    const tripWithHotel = data.options.find((option) =>
      option.relatedHotelIds?.includes(preferredHotelId)
    );

    if (!tripWithHotel) {
      return;
    }

    setSelectedTripId(tripWithHotel.id);
    setSelectedHotelId(preferredHotelId);
    setSchedule(
      preferredSchedule ?? createDefaultStaySchedule(
        tripWithHotel.minDays ?? 1,
        tripWithHotel.maxDays ?? 14
      )
    );
    setStep('panel');
  }, [location.state, data]);

  function handleSelectTrip(option) {
    const minDays = option.minDays ?? 1;
    const maxDays = option.maxDays ?? 14;
    const nights = clampNights(option.defaultDays ?? minDays, minDays, maxDays);
    const baseSchedule = createDefaultStaySchedule(minDays, maxDays);

    setSelectedTripId(option.id);
    setSchedule({
      ...baseSchedule,
      nights,
      checkOutDate: addDaysToDate(baseSchedule.checkInDate, nights),
    });
    setSelectedHotelId(null);
    setConfirmation(null);
    setStep('panel');
  }

  function handleBackToGrid() {
    setSelectedTripId(null);
    setSelectedHotelId(null);
    setStep('grid');
  }

  function resetBooking() {
    setSelectedTripId(null);
    setSelectedHotelId(null);
    setConfirmation(null);
    setStep('grid');
  }

  if (loading) return <LoadingSpinner label="Loading booking options…" />;

  if (error) {
    return (
      <div className="container">
        <ErrorMessage message={error} onRetry={refetch} />
      </div>
    );
  }

  const { options, partners } = data;

  return (
    <div className="container">
      <header className="page-header">
        <p className="section-label">Reservations</p>
        <h1>Book a trip</h1>
        <p>
          Select an activity or package, choose your stay duration, compare live hotel prices,
          and complete payment securely on this site via Razorpay.
        </p>
      </header>

      <BookingStepper currentStep={step} />

      <div className="booking-disclaimer">
        <strong>Before you pay:</strong> Verify operator registration with the Tourist Reception
        Centre (TRC), Residency Road, Srinagar. Report unregistered touts to Tourist Police: 0194-245-2000.
      </div>

      {step === 'grid' && (
        <div className="booking-grid">
          {options.map((option) => (
            <article key={option.id} className="booking-card">
              {option.badge && <span className="booking-badge">{option.badge}</span>}
              <div className="booking-card-body">
                <h3>{option.title}</h3>
                <p className="booking-provider">{option.provider}</p>
                <div className="booking-meta">
                  <span className="booking-meta-item">{option.price}</span>
                  <span className="booking-meta-item">{option.duration}</span>
                </div>
                <p className="booking-desc">{option.description}</p>
                <ul className="booking-features">
                  <li>Live hotel prices</li>
                  <li>Secure Razorpay checkout</li>
                  <li>{option.minDays}–{option.maxDays} night options</li>
                </ul>
              </div>
              <button
                type="button"
                className="btn btn-saffron booking-btn"
                onClick={() => handleSelectTrip(option)}
              >
                Book now →
              </button>
            </article>
          ))}
        </div>
      )}

      {step === 'panel' && selectedTripId && (
        <TripBookingPanel
          tripId={selectedTripId}
          schedule={schedule}
          selectedHotelId={selectedHotelId}
          onScheduleChange={setSchedule}
          onHotelSelect={setSelectedHotelId}
          onContinue={() => setStep('checkout')}
          onBack={handleBackToGrid}
        />
      )}

      {step === 'checkout' && selectedTrip && selectedHotel && (
        <BookingCheckout
          trip={selectedTrip}
          hotel={selectedHotel}
          schedule={schedule}
          livePrice={selectedLivePrice}
          onBack={() => setStep('panel')}
          onConfirmed={(result) => {
            setConfirmation(result);
            setStep('confirmation');
          }}
        />
      )}

      {step === 'confirmation' && confirmation && (
        <BookingConfirmation confirmation={confirmation} onBookAnother={resetBooking} />
      )}

      {step === 'grid' && (
        <section className="partners-section">
          <h2>Booking partners</h2>
          <p>Prefer an external platform? These sites also list verified properties and packages for J&K.</p>
          <div className="partners-row">
            {partners.map((p) => (
              <a
                key={p.name}
                href={p.url}
                target="_blank"
                rel="noopener noreferrer"
                className="partner-link"
              >
                {p.name} ↗
              </a>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
