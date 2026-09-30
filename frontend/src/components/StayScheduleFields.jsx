import {
  addDaysToDate,
  clampNights,
  computeNightsBetween,
  formatStayDate,
  formatStayTime,
  todayDateString,
} from '../utils/booking';

export default function StayScheduleFields({
  schedule,
  onChange,
  minNights = 1,
  maxNights = 30,
  idPrefix = 'stay',
}) {
  const today = todayDateString();
  const minCheckOut = addDaysToDate(schedule.checkInDate || today, minNights);
  const maxCheckOut = addDaysToDate(schedule.checkInDate || today, maxNights);

  // Merge schedule updates and notify the parent component.
  function update(partial) {
    onChange({ ...schedule, ...partial });
  }

  // When check-in date changes, keep the same night count and move check-out accordingly.
  function handleCheckInDateChange(checkInDate) {
    const nights = clampNights(schedule.nights, minNights, maxNights);
    update({
      checkInDate,
      checkOutDate: addDaysToDate(checkInDate, nights),
    });
  }

  // When the check-out date changes, recompute nights based on the new range.
  function handleCheckOutDateChange(checkOutDate) {
    const nights = computeNightsBetween(schedule.checkInDate, checkOutDate);
    update({
      checkOutDate,
      nights: clampNights(nights, minNights, maxNights),
    });
  }

  // When nights change directly, update the checkout date to match.
  function handleNightsChange(rawNights) {
    const nights = clampNights(rawNights, minNights, maxNights);
    update({
      nights,
      checkOutDate: addDaysToDate(schedule.checkInDate || today, nights),
    });
  }

  return (
    <div className="stay-schedule">
      <div className="stay-schedule-row stay-schedule-row--dates">
        <label htmlFor={`${idPrefix}-check-in-date`} className="stay-field">
          <span className="stay-field-label">Check-in</span>
          <input
            id={`${idPrefix}-check-in-date`}
            type="date"
            min={today}
            value={schedule.checkInDate}
            onChange={(event) => handleCheckInDateChange(event.target.value)}
            className="stay-input"
          />
          <input
            id={`${idPrefix}-check-in-time`}
            type="time"
            value={schedule.checkInTime}
            onChange={(event) => update({ checkInTime: event.target.value })}
            className="stay-input stay-input--time"
            aria-label="Check-in time"
          />
        </label>

        <label htmlFor={`${idPrefix}-check-out-date`} className="stay-field">
          <span className="stay-field-label">Check-out</span>
          <input
            id={`${idPrefix}-check-out-date`}
            type="date"
            min={minCheckOut}
            max={maxCheckOut}
            value={schedule.checkOutDate}
            onChange={(event) => handleCheckOutDateChange(event.target.value)}
            className="stay-input"
          />
          <input
            id={`${idPrefix}-check-out-time`}
            type="time"
            value={schedule.checkOutTime}
            onChange={(event) => update({ checkOutTime: event.target.value })}
            className="stay-input stay-input--time"
            aria-label="Check-out time"
          />
        </label>
      </div>

      <label htmlFor={`${idPrefix}-nights`} className="stay-field stay-field--nights">
        <span className="stay-field-label">Nights</span>
        <input
          id={`${idPrefix}-nights`}
          type="number"
          min={minNights}
          max={maxNights}
          step="1"
          inputMode="numeric"
          value={schedule.nights}
          onChange={(event) => handleNightsChange(event.target.value)}
          className="stay-input stay-input--nights"
        />
        <span className="stay-field-hint">
          {minNights}–{maxNights} nights allowed
        </span>
      </label>

      {/* Summary text uses formatted dates/times so the user can confirm the selected stay. */}
      <p className="stay-schedule-summary">
        {formatStayDate(schedule.checkInDate)} at {formatStayTime(schedule.checkInTime)}
        {' → '}
        {formatStayDate(schedule.checkOutDate)} at {formatStayTime(schedule.checkOutTime)}
        {' · '}
        <strong>{schedule.nights} night{schedule.nights !== 1 ? 's' : ''}</strong>
      </p>
    </div>
  );
}
