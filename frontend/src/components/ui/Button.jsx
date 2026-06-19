import { cva } from 'class-variance-authority';
import { forwardRef } from 'react';
import { cn } from '../../utils/cn.js';
import Spinner from './Spinner.jsx';

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 rounded-md text-sm font-semibold transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:pointer-events-none disabled:opacity-60',
  {
    variants: {
      variant: {
        primary: 'bg-primary text-primary-foreground shadow-sm hover:bg-primary/90',
        secondary: 'bg-secondary text-secondary-foreground hover:bg-secondary/80',
        ghost: 'text-muted-foreground hover:bg-accent hover:text-accent-foreground',
        destructive: 'bg-destructive text-destructive-foreground hover:bg-destructive/90',
        outline: 'border border-border bg-background text-foreground hover:bg-secondary',
      },
      size: {
        sm: 'h-9 px-4',
        md: 'h-11 px-5',
        lg: 'h-12 px-6',
        icon: 'h-11 w-11',
      },
      fullWidth: {
        true: 'w-full',
        false: '',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'md',
      fullWidth: false,
    },
  }
);

const Button = forwardRef(function Button(
  { className, variant, size, fullWidth, loading = false, children, disabled, ...props },
  ref
) {
  return (
    <button
      ref={ref}
      className={cn(buttonVariants({ variant, size, fullWidth, className }))}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? <Spinner className="h-4 w-4 text-current" /> : null}
      {children}
    </button>
  );
});

export default Button;
