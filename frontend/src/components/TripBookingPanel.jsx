import { useMemo } from 'react';
import { useApi } from '../hooks/useApi';
import { useLivePrices } from '../hooks/useLivePrices';
import { api } from '../services/api';
import SafeImage from './SafeImage';
import StayScheduleFields from './StayScheduleFields';
import {
  availabilityClass,
  availabilityLabel,
  createDefaultStaySchedule,
  formatCurrency,
  formatStayDate,
  formatStayTime,
  trendSymbol,
} from '../utils/booking';

/**
 * Booking panel shown after a trip is selected.
 * It lets the user choose stay dates, compares live hotel prices,
 * and selects a hotel before continuing to checkout.
 */
export default function TripBookingPanel({
  tripId,
  schedule,
  selectedHotelId,
  onScheduleChange,
  onHotelSelect,
  onContinue,
  onBack,
}) {
  // Load the trip option details for the selected trip.
  const { data: trip, loading: tripLoading, error: tripError } = useApi(
    () => api.getBookingOption(tripId),
    [tripId]
  );

  const { data: hotels, loading: hotelsLoading, error: hotelsError } = useApi(
    () => api.getHotels(),
    []
  );

  // Only show hotels that are related to the current trip option.
  const relatedHotels = useMemo(() => {
    if (!trip || !hotels) return [];
    return hotels.filter((hotel) => trip.relatedHotelIds.includes(hotel.id));
  }, [trip, hotels]);

  // Poll live hotel prices for the trip's related properties while the trip is loaded.
  const {
    prices,
    secondsAgo,
    secondsUntilRefresh,
    loading: pricesLoading,
    refreshing,
    error: pricesError,
  } = useLivePrices(trip?.relatedHotelIds, schedule.nights, Boolean(trip));

  // Map live price results to hotel IDs for easy lookup inside the hotel list.
  const priceMap = useMemo(
    () => Object.fromEntries(prices.map((price) => [price.hotelId, price])),
    [prices]
  );

  const selectedLive = selectedHotelId ? priceMap[selectedHotelId] : null;
  // The user may only continue if a hotel is selected and that stay is not marked unavailable.
  const canContinue = selectedHotelId && selectedLive?.available !== false;

  if (tripLoading) {
    return (
      <section className="booking-panel booking-panel--loading">
        <p className="booking-panel-status">
          <span className="live-refresh-dot" aria-hidden="true" />
          Loading trip details…
        </p>
      </section>
    );
  }

  if (tripError) {
    return <p className="booking-panel-error">{tripError}</p>;
  }

  return (
    <section className="booking-panel">
      <div className="booking-panel-header">
        <button type="button" className="btn btn-outline booking-back-btn" onClick={onBack}>
          ← Change trip
        </button>
        <div className="booking-panel-title">
          <p className="section-label">Selected trip</p>
          <h2>{trip.title}</h2>
          <p className="booking-provider">{trip.provider}</p>
          <p className="booking-trip-meta">{trip.duration} · {trip.price}</p>
        </div>
      </div>

      <div className="booking-panel-section booking-duration-section">
        <div className="booking-duration-header">
          <h3>Your stay dates</h3>
          <p className="booking-panel-hint">
            Set check-in, check-out, and nights — prices update as you adjust.
          </p>
        </div>
        <StayScheduleFields
          schedule={schedule}
          onChange={onScheduleChange}
          minNights={trip.minDays}
          maxNights={trip.maxDays}
          idPrefix={`trip-${trip.id}`}
        />
      </div>

      <div className="booking-panel-section">
        <div className="live-prices-header">
          <div>
            <h3>Choose your stay</h3>
            <p className="booking-panel-hint">
              We continuously check each linked property on this site — no external redirects.
            </p>
          </div>
          <div className="live-prices-meta">
            <span className="live-monitor-badge">
              <span className="live-refresh-dot" aria-hidden="true" />
              Live
            </span>
            {refreshing && <span className="live-checking-label">Checking…</span>}
            {secondsAgo !== null && (
              <span className="live-prices-updated">Updated {secondsAgo}s ago</span>
            )}
            {secondsUntilRefresh !== null && !refreshing && (
              <span className="live-next-check">Next check in {secondsUntilRefresh}s</span>
            )}
          </div>
        </div>

        {/* Display the selected stay window summary above the hotel list. */}
        <p className="stay-window-summary">
          {formatStayDate(schedule.checkInDate)} at {formatStayTime(schedule.checkInTime)}
          {' → '}
          {formatStayDate(schedule.checkOutDate)} at {formatStayTime(schedule.checkOutTime)}
        </p>

        {(hotelsLoading || (pricesLoading && !prices.length)) && (
          <div className="hotel-price-skeletons">
            {[1, 2].map((n) => (
              <div key={n} className="hotel-price-skeleton" aria-hidden="true" />
            ))}
          </div>
        )}

        {(hotelsError || pricesError) && (
          <p className="booking-panel-error">{hotelsError || pricesError}</p>
        )}

        <div className="hotel-price-list">
          {relatedHotels.map((hotel) => {
            const livePrice = priceMap[hotel.id];
            const isSelected = selectedHotelId === hotel.id;
            const isUnavailable = livePrice && !livePrice.available;

            return (
              <label
                key={hotel.id}
                className={`hotel-price-card ${isSelected ? 'selected' : ''} ${isUnavailable ? 'unavailable' : ''}`}
              >
                <input
                  type="radio"
                  name="selectedHotel"
                  value={hotel.id}
                  checked={isSelected}
                  disabled={isUnavailable}
                  onChange={() => onHotelSelect(hotel.id)}
                />
                <div className="hotel-price-card-body">
                  <div className="hotel-price-card-main">
                    <div className="hotel-price-thumb">
                      <SafeImage src={hotel.image} alt="" loading="lazy" />
                    </div>
                    <div className="hotel-price-info">
                      <div className="hotel-price-top">
                        <div>
                          <strong>{hotel.name}</strong>
                          <p className="hotel-location">{hotel.location}</p>
                        </div>
                        <span className="hotel-type-badge">{hotel.type}</span>
                      </div>
                      {livePrice ? (
                        <>
                          <span className={`availability-badge ${availabilityClass(livePrice.availabilityStatus)}`}>
                            {availabilityLabel(livePrice.availabilityStatus)}
                            {livePrice.available && ` · ${livePrice.roomsLeft} left`}
                          </span>
                          <div className="live-price-row">
                            <span className={`live-price-amount ${livePrice.trend}`}>
                              {formatCurrency(livePrice.pricePerNight)}
                              <span className="live-price-unit"> / night</span>
                            </span>
                            <span className={`live-price-trend trend-${livePrice.trend}`}>
                              {trendSymbol(livePrice.trend)}
                            </span>
                            <span className="live-price-total">
                              Total: {formatCurrency(livePrice.totalPrice)} for {schedule.nights} night{schedule.nights !== 1 ? 's' : ''}
                            </span>
                          </div>
                        </>
                      ) : (
                        <p className="hotel-static-price">{hotel.priceRange}</p>
                      )}
                    </div>
                  </div>
                </div>
              </label>
            );
          })}
        </div>
      </div>

      {selectedLive && !selectedLive.available && (
        <p className="booking-panel-error booking-panel-error--inline">
          This stay is fully booked for your dates. Pick another hotel or wait for the next refresh.
        </p>
      )}

      {/* Continue button is only enabled when a valid hotel is selected. */}
      <button
        type="button"
        className="btn btn-saffron booking-continue-btn"
        disabled={!canContinue}
        onClick={onContinue}
      >
        Continue to checkout
      </button>
    </section>
  );
}
