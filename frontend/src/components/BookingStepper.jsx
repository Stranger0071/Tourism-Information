import { BOOKING_STEPS } from '../utils/booking';

export default function BookingStepper({ currentStep }) {
  const currentIndex = BOOKING_STEPS.findIndex((step) => step.id === currentStep);

  return (
    <nav className="booking-stepper" aria-label="Booking progress">
      <ol className="booking-stepper-list">
        {BOOKING_STEPS.map((step, index) => {
          const isComplete = index < currentIndex;
          const isCurrent = index === currentIndex;
          const state = isComplete ? 'complete' : isCurrent ? 'current' : 'upcoming';

          return (
            <li key={step.id} className={`booking-step booking-step--${state}`}>
              <span className="booking-step-marker" aria-hidden="true">
                {isComplete ? '✓' : index + 1}
              </span>
              <span className="booking-step-label">{step.label}</span>
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
