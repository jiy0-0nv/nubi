import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="container-narrow page text-center" style={{ paddingTop: 100 }}>
      <div className="denied-mark" aria-hidden="true">
        404
      </div>
      <h1>길이 끊겼습니다</h1>
      <p className="muted mt-16">
        이 곳에는 아무것도 없습니다.
        <br />
        해가 지기 전에 돌아가십시오.
      </p>
      <Link to="/" className="btn btn-primary mt-24">
        나가기
      </Link>
    </div>
  );
}
