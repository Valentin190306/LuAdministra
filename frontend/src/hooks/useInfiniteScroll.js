import { useState, useEffect, useRef, useCallback } from 'react';
import { api } from '../api/client';

export function useInfiniteScroll(buildUrl, { pageSize = 50 } = {}) {
  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);
  const loadingRef = useRef(false);
  const sentinelRef = useRef(null);
  const fetchPage = useCallback(async (pageNum) => {
    if (loadingRef.current) return;
    loadingRef.current = true;
    setLoading(true);
    setError(null);
    try {
      const url = buildUrl(pageNum, pageSize);
      const result = await api.get(url);
      setData((prev) => {
        if (pageNum === 0) return result.content;
        const existingIds = new Set((prev ?? []).map((d) => d.id));
        const newItems = result.content.filter((d) => !existingIds.has(d.id));
        return [...(prev ?? []), ...newItems];
      });
      setPage(result.page + 1);
      setHasMore(result.page + 1 < result.totalPages);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
      loadingRef.current = false;
    }
  }, [buildUrl, pageSize]);

  useEffect(() => {
    setData(null);
    setPage(0);
    setHasMore(true);
    setError(null);
    loadingRef.current = false;
    fetchPage(0);
  }, [fetchPage, refreshKey]);

  useEffect(() => {
    if (!hasMore || loading || loadingRef.current) return;
    const sentinel = sentinelRef.current;
    if (!sentinel) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loadingRef.current) {
          fetchPage(page);
        }
      },
      { threshold: 0.1 }
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasMore, loading, page, fetchPage]);

  const refetch = useCallback(() => {
    setRefreshKey((k) => k + 1);
  }, []);

  return { data, loading, hasMore, error, sentinelRef, refetch };
}
