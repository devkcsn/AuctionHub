import { useState, useEffect } from 'react';

export default function CountdownTimer({ endTime, compact = false, onEnd }) {
  const [timeLeft, setTimeLeft] = useState(calc());

  function calc() {
    const diff = new Date(endTime).getTime() - Date.now();
    if (diff <= 0) return { days: 0, hours: 0, minutes: 0, seconds: 0, expired: true };
    return {
      days: Math.floor(diff / 86400000),
      hours: Math.floor((diff / 3600000) % 24),
      minutes: Math.floor((diff / 60000) % 60),
      seconds: Math.floor((diff / 1000) % 60),
      expired: false,
    };
  }

  useEffect(() => {
    const t = setInterval(() => {
      const tl = calc();
      setTimeLeft(tl);
      if (tl.expired) { clearInterval(t); onEnd?.(); }
    }, 1000);
    return () => clearInterval(t);
  }, [endTime]);

  if (timeLeft.expired) return <span className="text-accent-600 font-medium text-xs">Ended</span>;

  const urgent = timeLeft.days === 0 && timeLeft.hours < 1;
  const pad = (n) => String(n).padStart(2, '0');

  if (compact) {
    if (timeLeft.days > 0) return <span className="text-gray-500 tabular-nums">{timeLeft.days}d {timeLeft.hours}h</span>;
    return (
      <span className={`tabular-nums ${urgent ? 'text-accent-600' : 'text-gray-500'}`}>
        {pad(timeLeft.hours)}:{pad(timeLeft.minutes)}:{pad(timeLeft.seconds)}
      </span>
    );
  }

  return (
    <div className="flex gap-1.5">
      {timeLeft.days > 0 && <Block value={timeLeft.days} label="Days" />}
      <Block value={timeLeft.hours} label="Hrs" urgent={urgent} />
      <Block value={timeLeft.minutes} label="Min" urgent={urgent} />
      <Block value={timeLeft.seconds} label="Sec" urgent={urgent} />
    </div>
  );
}

function Block({ value, label, urgent }) {
  return (
    <div className={`text-center w-14 py-2 rounded-md ${urgent ? 'bg-accent-50' : 'bg-gray-100'}`}>
      <div className={`text-lg font-bold tabular-nums ${urgent ? 'text-accent-700' : 'text-gray-900'}`}>
        {String(value).padStart(2, '0')}
      </div>
      <div className="text-[10px] uppercase tracking-wider text-gray-400">{label}</div>
    </div>
  );
}