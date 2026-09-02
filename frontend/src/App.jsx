import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { BookmarksProvider } from './context/BookmarksContext';
import { AdminRoute, GuestOnlyRoute, ProtectedRoute } from './components/RouteGuards';
import UserLayout from './components/UserLayout';

/* 사용자 영역 */
import HomePage from './pages/HomePage';
import RoomListPage from './pages/RoomListPage';
import RoomDetailPage from './pages/RoomDetailPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import FindAccountPage from './pages/FindAccountPage';
import ReservationPage from './pages/ReservationPage';
import ReservationResultPage from './pages/ReservationResultPage';
import MyPage from './pages/MyPage';
import EditProfilePage from './pages/EditProfilePage';
import BookingListPage from './pages/BookingListPage';
import BookingDetailPage from './pages/BookingDetailPage';
import BookmarkListPage from './pages/BookmarkListPage';
import NotFoundPage from './pages/NotFoundPage';
import ForbiddenPage from './pages/ForbiddenPage';
import WhoAmIPage from './pages/WhoAmIPage';

/* 관리자 영역 */
import AdminLayout from './pages/admin/AdminLayout';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminRoomsPage from './pages/admin/AdminRoomsPage';
import AdminRoomFormPage from './pages/admin/AdminRoomFormPage';
import AdminRoomDetailPage from './pages/admin/AdminRoomDetailPage';
import AdminRoomImagesPage from './pages/admin/AdminRoomImagesPage';
import AdminBookingsPage from './pages/admin/AdminBookingsPage';
import AdminBookingDetailPage from './pages/admin/AdminBookingDetailPage';

/* ------------------------------------------------------------------
 * 라우팅 구조 — 사용자 영역과 관리자 영역을 트리 자체로 분리했습니다.
 *
 *   /            ... UserLayout  (상단 헤더 + 푸터)
 *   /admin/*     ... AdminLayout (좌측 사이드바 셸, AdminRoute로 보호)
 *
 * 권한 판정은 AuthContext.isAdmin(= AccountResponseDTO.role === 'ADMIN') 하나로
 * 통일되어 있고, 로그인 직후 분기는 LoginPage에서 처리합니다.
 * ------------------------------------------------------------------ */
export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <BookmarksProvider>
          <Routes>
            {/* ============ 관리자 영역 ============ */}
            <Route
              path="/admin"
              element={
                <AdminRoute>
                  <AdminLayout />
                </AdminRoute>
              }
            >
              <Route index element={<AdminDashboardPage />} />
              <Route path="rooms" element={<AdminRoomsPage />} />
              <Route path="rooms/new" element={<AdminRoomFormPage />} />
              <Route path="rooms/:roomId" element={<AdminRoomDetailPage />} />
              <Route path="rooms/:roomId/edit" element={<AdminRoomFormPage />} />
              <Route path="rooms/:roomId/images" element={<AdminRoomImagesPage />} />
              <Route path="bookings" element={<AdminBookingsPage />} />
              <Route path="bookings/:bookingId" element={<AdminBookingDetailPage />} />
              <Route path="*" element={<Navigate to="/admin" replace />} />
            </Route>

            {/* ============ 사용자 영역 ============ */}
            <Route element={<UserLayout />}>
              <Route index element={<HomePage />} />
              <Route path="/rooms" element={<RoomListPage />} />
              <Route path="/rooms/:roomId" element={<RoomDetailPage />} />

              {/* 이미 로그인했다면 각자의 홈으로 돌려보냅니다 */}
              <Route
                path="/login"
                element={
                  <GuestOnlyRoute>
                    <LoginPage />
                  </GuestOnlyRoute>
                }
              />
              <Route
                path="/signup"
                element={
                  <GuestOnlyRoute>
                    <SignupPage />
                  </GuestOnlyRoute>
                }
              />
              <Route
                path="/find-account"
                element={
                  <GuestOnlyRoute>
                    <FindAccountPage />
                  </GuestOnlyRoute>
                }
              />

              <Route
                path="/booking/:roomId"
                element={
                  <ProtectedRoute>
                    <ReservationPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/booking/result"
                element={
                  <ProtectedRoute>
                    <ReservationResultPage />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/mypage"
                element={
                  <ProtectedRoute>
                    <MyPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/mypage/edit"
                element={
                  <ProtectedRoute>
                    <EditProfilePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/mypage/bookings"
                element={
                  <ProtectedRoute>
                    <BookingListPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/mypage/bookings/:bookingId"
                element={
                  <ProtectedRoute>
                    <BookingDetailPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/mypage/bookmarks"
                element={
                  <ProtectedRoute>
                    <BookmarkListPage />
                  </ProtectedRoute>
                }
              />

              {/* 권한이 왜 이렇게 판정됐는지 서버 응답 그대로 확인하는 진단 화면 */}
              <Route
                path="/whoami"
                element={
                  <ProtectedRoute>
                    <WhoAmIPage />
                  </ProtectedRoute>
                }
              />

              <Route path="/forbidden" element={<ForbiddenPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Route>
          </Routes>
        </BookmarksProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
