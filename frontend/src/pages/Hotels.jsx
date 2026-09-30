import { useState } from 'react';
import HotelCard from '../components/HotelCard';
import ErrorMessage from '../components/ErrorMessage';
import SkeletonCard from '../components/SkeletonCard';
import { useApi } from '../hooks/useApi';
import { api } from '../services/api';

export default function Hotels() {
  const [activeType, setActiveType] = useState('All');

  const { data: types } = useApi(() => api.getHotelTypes(), []);
  const { data: hotels, loading, error, refetch } = useApi(
    () => api.getHotels(activeType),
    [activeType]
  );

  return (
    <div className="container">
      <header className="page-header">
        <p className="section-label">Accommodation</p>
        <h1>Hotels, houseboats & stays</h1>
        <p>
          A mix of registered houseboats, hotels, and seasonal camps across the valley.
          Check live availability and prices right here — we monitor each property continuously.
        </p>
      </header>

      {types && (
        <div className="filter-bar">
          {types.map((type) => (
            <button
              key={type}
              type="button"
              className={`filter-btn ${activeType === type ? 'active' : ''}`}
              onClick={() => setActiveType(type)}
            >
              {type}
            </button>
          ))}
        </div>
      )}

      {loading && (
        <div className="cards-grid">
          {[1, 2, 3].map((n) => <SkeletonCard key={n} />)}
        </div>
      )}

      {error && <ErrorMessage message={error} onRetry={refetch} />}

      {!loading && !error && hotels && (
        <div className="cards-grid">
          {hotels.map((hotel) => (
            <HotelCard key={hotel.id} hotel={hotel} />
          ))}
        </div>
      )}
    </div>
  );
}
