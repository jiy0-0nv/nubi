export default function EmptyState({ mark = '†', title, description, action }) {
  return (
    <div className="empty">
      <div className="empty-mark" aria-hidden="true">
        {mark}
      </div>
      {title && <p className="serif" style={{ fontSize: 17, color: 'var(--bone)', marginBottom: 8 }}>{title}</p>}
      {description && <p className="tiny">{description}</p>}
      {action && <div className="mt-24">{action}</div>}
    </div>
  );
}
