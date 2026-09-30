import { useState } from 'react';
import { api } from '../services/api';
import { formatCurrency, formatStayDate, formatStayTime } from '../utils/booking';

/**
 * Dynamically load the Razorpay checkout script if it is not already present.
 * Returns the global Razorpay constructor once the script has loaded.
 */
function loadRazorpayScript() {
  return new Promise((resolve, reject) => {
    if (window.Razorpay) {
      resolve(window.Razorpay);
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    script.onload = () => resolve(window.Razorpay);
    script.onerror = () => reject(new Error('Failed to load Razorpay checkout'));
    document.body.appendChild(script);
  });
}

/**
 * Checkout panel for booking a trip, submitting guest details, and
 * launching Razorpay payment flow.
 */
export default function BookingCheckout({
  trip,
  hotel,
  schedule,
  livePrice,
  onBack,
  onConfirmed,
}) {
  // Local form state for checkout fields and validation state.
  const [form, setForm] = useState({
    guestName: '',
    guestEmail: '',
    guestPhone: '',
    guests: 2,
    checkInDate: schedule.checkInDate,
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  // Calculate the price displayed to the user. Prefer live pricing when available.
  const pricePerNight = livePrice?.pricePerNight ?? hotel.basePrice;
  const totalPrice = livePrice?.totalPrice ?? pricePerNight * schedule.nights;
  const today = new Date().toISOString().split('T')[0];

  // Helper to update a single form field without replacing the rest of the state.
  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      // Create a booking order on the backend and receive Razorpay order data.
      const order = await api.createBookingOrder({
        tripId: trip.id,
        hotelId: hotel.id,
        days: schedule.nights,
        guestName: form.guestName,
        guestEmail: form.guestEmail,
        guestPhone: form.guestPhone,
        guests: Number(form.guests),
        checkInDate: form.checkInDate,
      });

      const Razorpay = await loadRazorpayScript();

      const checkout = new Razorpay({
        key: order.keyId,
        amount: order.amount,
        currency: order.currency,
        name: 'J&K Tourism Portal',
        description: `${trip.title} — ${hotel.name}`,
        order_id: order.orderId,
        prefill: {
          name: form.guestName,
          email: form.guestEmail,
          contact: form.guestPhone,
        },
        theme: { color: '#c45c26' },
        handler: async (response) => {
          try {
            // Verify payment on the server after Razorpay completes checkout.
            const confirmation = await api.verifyBookingPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            });
            onConfirmed(confirmation);
          } catch (verifyError) {
            setError(verifyError.message || 'Payment verification failed');
            setSubmitting(false);
          }
        },
        modal: {
          ondismiss: () => {
            // Restore button state when the checkout modal is closed.
            setSubmitting(false);
          },
        },
      });

      checkout.on('payment.failed', (response) => {
        setError(response.error?.description || 'Payment failed');
        setSubmitting(false);
      });

      checkout.open();
    } catch (submitError) {
      // Show an error when order creation, script loading, or checkout setup fails.
      setError(submitError.message || 'Could not start checkout');
      setSubmitting(false);
    }
  }

  return (
    <section className="booking-checkout">
      <div className="booking-panel-header">
        <button type="button" className="btn btn-outline booking-back-btn" onClick={onBack}>
          ← Back to hotels
        </button>
        <div className="booking-panel-title">
          <p className="section-label">Checkout</p>
          <h2>{trip.title}</h2>
          <p className="booking-provider">
            {hotel.name} · {schedule.nights} night{schedule.nights !== 1 ? 's' : ''}
          </p>
        </div>
      </div>

      <div className="checkout-layout">
        {/* Summary panel shows the trip, hotel, dates, and total price locked at checkout. */}
        <div className="checkout-summary-card">
          <h3>Order summary</h3>
          <div className="checkout-summary-lines">
            <p><span>Trip</span><strong>{trip.title}</strong></p>
            <p><span>Stay</span><strong>{hotel.name}</strong></p>
            <p><span>Location</span><strong>{hotel.location}</strong></p>
            <p><span>Check-in</span><strong>{formatStayDate(schedule.checkInDate)} · {formatStayTime(schedule.checkInTime)}</strong></p>
            <p><span>Check-out</span><strong>{formatStayDate(schedule.checkOutDate)} · {formatStayTime(schedule.checkOutTime)}</strong></p>
            <p><span>Nights</span><strong>{schedule.nights}</strong></p>
          </div>
          <div className="checkout-price-lock">
            <span className="live-refresh-dot" aria-hidden="true" />
            Price locked at checkout — based on latest live rate
          </div>
          <div className="checkout-total-row">
            <span>{formatCurrency(pricePerNight)} × {schedule.nights} nights</span>
            <strong>{formatCurrency(totalPrice)}</strong>
          </div>
        </div>

        {/* Guest details form collects payment and stay information before checkout. */}
        <form className="checkout-form" onSubmit={handleSubmit}>
          <h3>Guest details</h3>

          <label>
            Full name
            <input
              type="text"
              required
              autoComplete="name"
              placeholder="As on ID"
              value={form.guestName}
              onChange={(event) => updateField('guestName', event.target.value)}
            />
          </label>

          <label>
            Email
            <input
              type="email"
              required
              autoComplete="email"
              placeholder="you@example.com"
              value={form.guestEmail}
              onChange={(event) => updateField('guestEmail', event.target.value)}
            />
          </label>

          <label>
            Phone
            <input
              type="tel"
              required
              autoComplete="tel"
              placeholder="+91 …"
              value={form.guestPhone}
              onChange={(event) => updateField('guestPhone', event.target.value)}
            />
          </label>

          <div className="checkout-form-row">
            <label>
              Guests
              <input
                type="number"
                min="1"
                max="20"
                required
                value={form.guests}
                onChange={(event) => updateField('guests', event.target.value)}
              />
            </label>

            <label>
              Check-in date
              <input
                type="date"
                required
                min={today}
                value={form.checkInDate}
                onChange={(event) => updateField('checkInDate', event.target.value)}
              />
            </label>
          </div>

          {error && <p className="booking-panel-error">{error}</p>}

          <button type="submit" className="btn btn-saffron booking-continue-btn" disabled={submitting}>
            {submitting ? 'Opening Razorpay…' : `Pay ${formatCurrency(totalPrice)} securely`}
          </button>

          <p className="checkout-secure-note">
            Payments processed via Razorpay. Your card details never touch this site.
          </p>
        </form>
      </div>
    </section>
  );
}
