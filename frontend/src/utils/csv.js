const BOM = '\uFEFF';

function escape(val) {
  const str = String(val ?? '');
  return `"${str.replace(/"/g, '""')}"`;
}

export function downloadCSV(data, columns, filename) {
  if (!data || data.length === 0) return;

  const headers = columns.map((c) => c.label).join(',');
  const rows = data
    .map((row) =>
      columns.map((col) => escape(col.value ? col.value(row) : row[col.key])).join(',')
    )
    .join('\n');

  const blob = new Blob([BOM + headers + '\n' + rows], {
    type: 'text/csv;charset=utf-8;',
  });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}
