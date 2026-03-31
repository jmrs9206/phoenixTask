import type { ReactNode } from "react";

type PageHeaderProps = {
  title: string;
  subtitle?: ReactNode;
};

export default function PageHeader({ title, subtitle }: PageHeaderProps) {
  return (
    <div className="page-header">
      <h2>{title}</h2>
      {subtitle ? <p className="page-subtitle">{subtitle}</p> : null}
    </div>
  );
}
