import { useState, useEffect, useCallback } from 'react';
import { api } from '../api/client';

function buildQuery(params) {
  if (!params) return '';
  const filtered = Object.entries(params).filter(([, v]) => v !== '' && v != null);
  if (filtered.length === 0) return '';
  return '?' + new URLSearchParams(filtered).toString();
}

export function useApi(path, params) {
  const fullPath = path + buildQuery(params);

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const refetch = useCallback(() => {
    setLoading(true);
    setError(null);
    api.get(fullPath)
      .then(setData)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [fullPath]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { data, loading, error, refetch };
}
