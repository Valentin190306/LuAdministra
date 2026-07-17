import styles from './Button.module.css';

export default function Button({ children, variant = 'primary', onClick, type = 'button', disabled, className }) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={`${styles.btn} ${styles[variant]} ${className ?? ''}`}
    >
      {children}
    </button>
  );
}
