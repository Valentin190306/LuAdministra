import { useState, useEffect, useCallback, useRef } from 'react';
import { api } from '../api/client';

function buildQuery(params) {
  if (!params) return '';
  const filtered = Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined);
  if (filtered.length === 0) return '';
  return '?' + new URLSearchParams(filtered).toString();
}

export function useApi(path, params) {
  const fullPath = path + buildQuery(params);

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const abortRef = useRef(null);

  const refetch = useCallback(() => {
    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setLoading(true);
    setError(null);
    api.get(fullPath, { signal: controller.signal })
      .then(setData)
      .catch((err) => { if (err.name !== 'AbortError') setError(err); })
      .finally(() => setLoading(false));
  }, [fullPath]);

  useEffect(() => {
    refetch();
    return () => abortRef.current?.abort();
  }, [fullPath]);

  return { data, loading, error, refetch };
}
