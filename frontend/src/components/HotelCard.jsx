import SafeImage from './SafeImage';
import HotelAvailability from './HotelAvailability';
import './HotelCard.css';

export default function HotelCard({ hotel }) {
  return (
    <article className="hotel-card">
      <div className="hotel-image-wrap">
        <SafeImage src={hotel.image} alt={hotel.name} loading="lazy" />
        <span className="hotel-type-badge">{hotel.type}</span>
      </div>
      <div className="hotel-body">
        <div className="hotel-top-row">
          <h3>{hotel.name}</h3>
          <span className="hotel-rating" aria-label={`${hotel.rating} out of 5`}>
            {hotel.rating}
          </span>
        </div>
        <p className="hotel-location">{hotel.location}</p>
        <p className="hotel-price">{hotel.priceRange}</p>
        <ul className="hotel-amenities">
          {hotel.amenities.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
        {hotel.note && <p className="hotel-note">{hotel.note}</p>}
        <div className="hotel-actions">
          <HotelAvailability hotel={hotel} />
        </div>
      </div>
    </article>
  );
}
