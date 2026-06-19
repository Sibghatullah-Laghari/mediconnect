import { cn } from '../../utils/cn.js';

export default function Spinner({ className }) {
  return (
    <span
      className={cn(
        'inline-block h-5 w-5 animate-spin rounded-full border-2 border-current border-r-transparent',
        className
      )}
      aria-hidden="true"
    />
  );
}
