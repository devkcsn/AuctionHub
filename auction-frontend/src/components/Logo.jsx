/**
 * AuctionHub logo component — rising-A mark + wordmark.
 *
 * @param {"icon"|"full"} variant  "icon" = mark only, "full" = mark + wordmark
 * @param {"light"|"dark"} theme   "light" = turquoise mark on dark bg,  "dark" = deeper mark on light bg
 * @param {number} size            height of the icon mark in px (default 28)
 */
export default function Logo({ variant = 'full', theme = 'light', size = 28 }) {
  const mark = theme === 'light' ? '#73eedc' : '#098578';
  const wordColor = theme === 'light' ? '#ffffff' : '#18181b';
  const wordMuted = theme === 'light' ? '#a1a1aa' : '#71717a';

  return (
    <span className="inline-flex items-center gap-2 select-none" aria-label="AuctionHub">
      {/* ---- Icon mark ---- */}
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 512 512"
        width={size}
        height={size}
        className="shrink-0"
        role="img"
        aria-hidden="true"
      >
        {/* Rising A / upward bid arrow */}
        <path
          d="M100 380 L256 90 L412 380"
          stroke={mark}
          strokeWidth="52"
          strokeLinecap="round"
          strokeLinejoin="round"
          fill="none"
        />
        {/* A crossbar — price level */}
        <line
          x1="164"
          y1="284"
          x2="348"
          y2="284"
          stroke={mark}
          strokeWidth="42"
          strokeLinecap="round"
        />
        {/* Hub dot */}
        <circle cx="256" cy="446" r="30" fill={mark} />
      </svg>

      {/* ---- Wordmark ---- */}
      {variant === 'full' && (
        <span className="flex items-baseline gap-0.5 leading-none" style={{ fontFamily: "'DM Sans', sans-serif" }}>
          <span
            className="font-bold tracking-tight"
            style={{ color: wordColor, fontSize: size * 0.64 }}
          >
            Auction
          </span>
          <span
            className="font-medium tracking-tight"
            style={{ color: wordMuted, fontSize: size * 0.64 }}
          >
            Hub
          </span>
        </span>
      )}
    </span>
  );
}
