import Modal from './Modal';
import Button from './Button';

export default function ConfirmDialog({ isOpen, onClose, onConfirm, title, message }) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title ?? 'Confirmar'}>
      <p style={{ marginBottom: 'var(--spacing-lg)', fontSize: '1.4rem' }}>{message}</p>
      <div style={{ display: 'flex', gap: 'var(--spacing-sm)', justifyContent: 'flex-end' }}>
        <Button variant="ghost" onClick={onClose}>Cancelar</Button>
        <Button variant="danger" onClick={onConfirm}>Eliminar</Button>
      </div>
    </Modal>
  );
}
