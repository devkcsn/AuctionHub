export default function LoadingSpinner({ size = 'md', text }) {
  const s = { sm: 'w-5 h-5', md: 'w-7 h-7', lg: 'w-10 h-10' };
  return (
    <div className="flex flex-col items-center justify-center py-16">
      <div className={`${s[size]} border-2 border-gray-200 border-t-primary-600 rounded-full animate-spin`} />
      {text && <p className="mt-3 text-gray-400 text-xs">{text}</p>}
    </div>
  );
}