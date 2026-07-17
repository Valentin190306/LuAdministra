import { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';

export function useApi(path) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refetch = useCallback(() => {
    setLoading(true);
    setError(null);
    api.get(path)
      .then(setData)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [path]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { data, loading, error, refetch };
}
