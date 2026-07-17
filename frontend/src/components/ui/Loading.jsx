import styles from './Loading.module.css';

export default function Loading({ text = 'Cargando...' }) {
  return <p className={styles.loading}>{text}</p>;
}
