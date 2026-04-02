type AvatarBadgeProps = {
  name: string;
  size?: "sm" | "md";
};

function getInitials(name: string) {
  if (!name || name.trim().toLowerCase() === "unassigned") {
    return "—";
  }
  const parts = name.trim().split(/\s+/);
  const first = parts[0]?.[0] ?? "";
  const last = parts.length > 1 ? parts[parts.length - 1][0] ?? "" : "";
  const initials = `${first}${last}`.toUpperCase();
  return initials || "•";
}

export default function AvatarBadge({ name, size = "sm" }: AvatarBadgeProps) {
  const initials = getInitials(name);
  const isUnassigned = initials === "—";
  return (
    <span className={`assignee-avatar ${size} ${isUnassigned ? "is-unassigned" : ""}`} title={name}>
      {initials}
    </span>
  );
}
