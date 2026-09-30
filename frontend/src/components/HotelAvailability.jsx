import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLivePrices } from '../hooks/useLivePrices';
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

export default function HotelAvailability({ hotel }) {
  const [open, setOpen] = useState(false);
  const [schedule, setSchedule] = useState(() => createDefaultStaySchedule(1, 14));

  // Fetch live hotel prices only while the availability panel is open.
  const { prices, secondsAgo, secondsUntilRefresh, loading, error, refreshing } = useLivePrices(
    [hotel.id],
    schedule.nights,
    open
  );

  const live = prices[0];

  // Toggle showing/hiding the live availability panel.
  function togglePanel() {
    setOpen((current) => !current);
  }

  return (
    <div className="hotel-availability">
      <button
        type="button"
        className={`btn ${open ? 'btn-outline' : 'btn-saffron'} hotel-avail-btn`}
        onClick={togglePanel}
        aria-expanded={open}
        aria-controls={`availability-${hotel.id}`}
      >
        {open ? 'Stop monitoring' : 'Check availability'}
      </button>

      {open && (
        <div
          id={`availability-${hotel.id}`}
          className="hotel-avail-panel"
          role="region"
          aria-label={`Live availability for ${hotel.name}`}
          aria-live="polite"
        >
          {/* Live monitoring header with refresh state and timing info. */}
          <div className="hotel-avail-panel-header">
            <div>
              <strong>Monitoring {hotel.name}</strong>
              <p className="hotel-avail-subtitle">
                Checking this property every 15 seconds.
              </p>
            </div>
            <div className="hotel-avail-meta">
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

          <StayScheduleFields
            schedule={schedule}
            onChange={setSchedule}
            minNights={1}
            maxNights={14}
            idPrefix={`hotel-${hotel.id}`}
          />

          {loading && !live && (
            <div className="hotel-avail-loading">
              <span className="live-refresh-dot" aria-hidden="true" />
              Looking up rooms for {formatStayDate(schedule.checkInDate)}…
            </div>
          )}

          {error && <p className="booking-panel-error">{error}</p>}

          {live && (
            // Show the latest live availability and price details when available.
            <div className={`hotel-avail-result ${refreshing ? 'hotel-avail-result--refreshing' : ''}`}>
              <span className={`availability-badge ${availabilityClass(live.availabilityStatus)}`}>
                {availabilityLabel(live.availabilityStatus)}
              </span>

              {live.available ? (
                <p className="hotel-avail-rooms">
                  {live.roomsLeft} room{live.roomsLeft !== 1 ? 's' : ''} left for{' '}
                  {formatStayDate(schedule.checkInDate)} – {formatStayDate(schedule.checkOutDate)}
                </p>
              ) : (
                <p className="hotel-avail-rooms hotel-avail-rooms--none">
                  No rooms for these dates right now — we&apos;ll keep checking automatically.
                </p>
              )}

              <p className="hotel-avail-times">
                Check-in {formatStayTime(schedule.checkInTime)} · Check-out {formatStayTime(schedule.checkOutTime)}
              </p>

              <div className="live-price-row">
                <span className={`live-price-amount ${live.trend}`}>
                  {formatCurrency(live.pricePerNight)}
                  <span className="live-price-unit"> / night</span>
                </span>
                <span className={`live-price-trend trend-${live.trend}`}>
                  {trendSymbol(live.trend)}
                </span>
                <span className="live-price-total">
                  Total: {formatCurrency(live.totalPrice)} for {schedule.nights} night{schedule.nights !== 1 ? 's' : ''}
                </span>
              </div>
            </div>
          )}

          <div className="hotel-avail-actions">
            <Link
              to="/booking"
              state={{
                preferredHotelId: hotel.id,
                staySchedule: schedule,
              }}
              className="btn btn-primary"
            >
              Book this stay
            </Link>
            <p className="hotel-avail-note">
              Availability updates while this panel stays open. Close it to stop monitoring.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
