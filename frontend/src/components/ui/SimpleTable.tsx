import type { ReactNode } from "react";
import EmptyState from "./EmptyState";

type Column<T> = {
  key?: string;
  header: string;
  render: (row: T) => ReactNode;
  width?: string;
};

type SimpleTableProps<T> = {
  columns: Column<T>[];
  rows: T[];
  emptyTitle?: string;
  emptyMessage?: string;
};

export default function SimpleTable<T>({
  columns,
  rows,
  emptyTitle,
  emptyMessage
}: SimpleTableProps<T>) {
  if (rows.length === 0) {
    return (
      <div className="table-wrapper">
        <EmptyState title={emptyTitle} message={emptyMessage} size="compact" />
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <table className="simple-table">
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.key ?? column.header} style={column.width ? { width: column.width } : undefined}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, rowIndex) => (
            <tr key={(row as { id?: number }).id ?? rowIndex}>
              {columns.map((column) => (
                <td key={column.key ?? column.header} data-label={column.header}>
                  {column.render(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
