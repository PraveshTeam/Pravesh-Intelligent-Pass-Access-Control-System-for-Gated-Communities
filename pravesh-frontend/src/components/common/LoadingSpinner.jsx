export default function LoadingSpinner({ fullPage = false, text = 'Loading...' }) {
  const content = (
    <div className="d-flex flex-column align-items-center justify-content-center py-5">
      <div className="pravesh-spinner mb-3"></div>
      <p className="text-muted small mb-0">{text}</p>
    </div>
  )

  if (fullPage) {
    return (
      <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light">
        {content}
      </div>
    )
  }
  return content
}