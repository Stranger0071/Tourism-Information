import { formatCurrency } from '../utils/booking';

export default function BookingConfirmation({ confirmation, onBookAnother }) {
  const confirmedDate = new Date(confirmation.confirmedAt).toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });

  return (
    <section className="booking-confirmation">
      <div className="confirmation-hero">
        <span className="confirmation-icon" aria-hidden="true">✓</span>
        <p className="section-label">Booking confirmed</p>
        <h2>Thank you, {confirmation.guestName}</h2>
        <p className="confirmation-id">
          Booking ID: <strong>{confirmation.bookingId}</strong>
        </p>
      </div>

      <div className="confirmation-card">
        <p><span>Trip</span><strong>{confirmation.tripTitle}</strong></p>
        <p><span>Hotel</span><strong>{confirmation.hotelName}</strong></p>
        <p><span>Duration</span><strong>{confirmation.days} night{confirmation.days !== 1 ? 's' : ''}</strong></p>
        <p><span>Check-in</span><strong>{confirmation.checkInDate}</strong></p>
        <p><span>Guests</span><strong>{confirmation.guests}</strong></p>
        <p className="confirmation-total"><span>Total paid</span><strong>{formatCurrency(confirmation.totalPrice)}</strong></p>
        <p><span>Confirmed at</span><strong>{confirmedDate}</strong></p>
      </div>

      <p className="booking-panel-hint">
        A confirmation has been recorded. Present your booking ID at the Tourist Reception Centre if needed.
      </p>

      <button type="button" className="btn btn-saffron booking-continue-btn" onClick={onBookAnother}>
        Book another trip
      </button>
    </section>
  );
}
