import * as XLSX from 'xlsx';

function rowsFromData(data, columns) {
  return [
    columns.map((c) => c.label),
    ...data.map((row) =>
      columns.map((col) => (col.value ? col.value(row) : row[col.key]))
    ),
  ];
}

export function exportExcel(sheets, filename) {
  const wb = XLSX.utils.book_new();
  for (const [name, { columns, data }] of Object.entries(sheets)) {
    const ws = XLSX.utils.aoa_to_sheet(rowsFromData(data ?? [], columns));
    ws['!cols'] = columns.map((c) => ({ wch: c.width ?? 18 }));
    XLSX.utils.book_append_sheet(wb, ws, name.slice(0, 31));
  }
  XLSX.writeFile(wb, filename);
}
