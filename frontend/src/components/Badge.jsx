/** tone: confirmed | completed | cancelled | blood | muted */
export default function Badge({ tone = 'muted', children }) {
  const cls = tone && tone !== 'muted' ? `badge badge-${tone}` : 'badge';
  return <span className={cls}>{children}</span>;
}
