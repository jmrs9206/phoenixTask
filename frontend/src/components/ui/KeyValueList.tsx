import type { ReactNode } from "react";

type KeyValueItem = {
  label: string;
  value: ReactNode;
};

type KeyValueListProps = {
  items: KeyValueItem[];
};

export default function KeyValueList({ items }: KeyValueListProps) {
  return (
    <div className="kv-grid">
      {items.map((item) => (
        <div className="kv-item" key={item.label}>
          <span className="kv-label">{item.label}</span>
          <span className="kv-value">{item.value}</span>
        </div>
      ))}
    </div>
  );
}
