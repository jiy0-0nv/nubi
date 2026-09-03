-- ============================================================
-- 숙소 사진(room_images) 시딩  -- 실제 rooms 데이터 기준
--
-- 전제 1) V2__add_sort_order_to_room_images.sql 실행 완료 (sort_order 컬럼 존재)
-- 전제 2) backend\copy-seed-images.ps1 실행 완료
--         -> backend\uploads\seed\ 에 60장이 영문명으로 복사되어 있어야 합니다.
--            DB 에는 경로 문자열만 들어가므로, 파일이 없으면 화면에 안 뜹니다.
--
-- 대상: room id 1~10 (실제 숙소). 숙소 1곳당 6장, 1번이 대표 사진.
--       id 12, 14, 15 는 테스트용 숙소라 사진을 넣지 않습니다.
--
-- 참고: 사진 파일명은 '장군총' 인데 숙소명은 '장수왕릉' 입니다.
--       장군총이 곧 장수왕릉으로 추정되는 무덤이라 room 8 에 연결했습니다.
-- ============================================================

USE accommodation_db;


-- ------------------------------------------------------------
-- STEP 0. 매핑 검증  ← 반드시 먼저 실행하세요
--
-- check_result 가 10행 모두 'OK' 여야 합니다.
-- 'NAME MISMATCH' 가 뜨면 그 room id 의 숙소가 제가 알던 것과 다르다는 뜻,
-- 'ROOM NOT FOUND' 면 그 id 의 숙소가 삭제되었다는 뜻입니다.
-- ------------------------------------------------------------
SELECT m.room_id,
       m.expected_name,
       r.name AS actual_name,
       m.slug,
       CASE
           WHEN r.id IS NULL          THEN 'ROOM NOT FOUND'
           WHEN r.name = m.expected_name THEN 'OK'
           ELSE 'NAME MISMATCH'
       END AS check_result
FROM (
    SELECT  1 AS room_id, '종묘'         AS expected_name, 'jongmyo'        AS slug UNION ALL
    SELECT  2,            '선정릉',                        'seonjeongneung'        UNION ALL
    SELECT  3,            '기자 피라미드',                 'pyramid'               UNION ALL
    SELECT  4,            '타지마할',                      'tajmahal'              UNION ALL
    SELECT  5,            '강화도 고인돌',                 'dolmen'                UNION ALL
    SELECT  6,            '무령왕릉',                      'muryeong'              UNION ALL
    SELECT  7,            '진시황릉',                      'jinshihuang'           UNION ALL
    SELECT  8,            '장수왕릉',                      'janggunchong'          UNION ALL
    SELECT  9,            '폼페이 유적',                   'pompeii'               UNION ALL
    SELECT 10,            '로마 카타콤',                   'catacomb'
) m
LEFT JOIN rooms r ON r.id = m.room_id
ORDER BY m.room_id;


-- ------------------------------------------------------------
-- STEP 1. 대상 숙소의 기존 사진 행 정리
--
-- 여러 번 실행해도 사진이 중복되지 않도록 먼저 지웁니다.
-- (DB 행만 지웁니다. 디스크의 파일은 그대로 남습니다)
-- ------------------------------------------------------------
DELETE FROM room_images
WHERE room_id BETWEEN 1 AND 10
  AND id > 0;   -- Workbench safe update mode 대응 (PK 조건)


-- ------------------------------------------------------------
-- STEP 2. 사진 60행 INSERT (숙소 10곳 x 6장)
--
-- url          : /uploads/seed/jongmyo1.png ...
-- is_thumbnail : 1번 사진만 1  -> 공개 목록(GET /api/rooms)의 thumbnailUrl
-- sort_order   : 0,1,2,3,4,5   -> 상세 화면 사진 순서
--
-- rooms 와 JOIN 하므로 실제로 존재하는 숙소에만 들어갑니다(FK 안전).
-- ------------------------------------------------------------
INSERT INTO room_images (room_id, url, is_thumbnail, sort_order)
SELECT r.id,
       CONCAT('/uploads/seed/', m.slug, n.seq, '.png'),
       (n.seq = 1),
       n.seq - 1
FROM (
    SELECT  1 AS room_id, 'jongmyo'        AS slug UNION ALL
    SELECT  2,            'seonjeongneung'        UNION ALL
    SELECT  3,            'pyramid'               UNION ALL
    SELECT  4,            'tajmahal'              UNION ALL
    SELECT  5,            'dolmen'                UNION ALL
    SELECT  6,            'muryeong'              UNION ALL
    SELECT  7,            'jinshihuang'           UNION ALL
    SELECT  8,            'janggunchong'          UNION ALL
    SELECT  9,            'pompeii'               UNION ALL
    SELECT 10,            'catacomb'
) m
JOIN rooms r ON r.id = m.room_id
JOIN (
    SELECT 1 AS seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL
    SELECT 4        UNION ALL SELECT 5 UNION ALL SELECT 6
) n
ORDER BY r.id, n.seq;

-- 60 rows affected 가 나와야 정상입니다.


-- ------------------------------------------------------------
-- STEP 3. 검증
-- ------------------------------------------------------------

-- 3-1. 숙소별 사진 수 / 대표 사진 (photos=6, thumbs=1 이면 정상)
SELECT r.id,
       r.name,
       COUNT(ri.id)             AS photos,
       SUM(ri.is_thumbnail = 1) AS thumbs,
       MIN(CASE WHEN ri.is_thumbnail = 1 THEN ri.url END) AS thumbnail_url
FROM rooms r
LEFT JOIN room_images ri ON ri.room_id = r.id
WHERE r.id BETWEEN 1 AND 10
GROUP BY r.id, r.name
ORDER BY r.id;

-- 3-2. 특정 숙소의 사진 순서 확인
-- SELECT id, room_id, sort_order, is_thumbnail, url
-- FROM room_images WHERE room_id = 1 ORDER BY sort_order;


-- ============================================================
-- 확인 방법
--   1) 이미지 직접 열기
--        http://localhost:8080/uploads/seed/jongmyo1.png
--   2) 목록 API 의 thumbnailUrl
--        GET http://localhost:8080/api/rooms?size=50
--   3) 상세 API 의 images 배열
--        GET http://localhost:8080/api/rooms/1
--
-- 이미지가 404 라면 uploads 폴더 위치 문제입니다.
-- WebUploadConfig 는 file.upload-dir(기본값 "uploads")을 실행 디렉터리 기준으로 잡으므로
-- 실행 방식에 따라 위치가 달라집니다. application.properties 에 절대경로로 못박으면 안전합니다:
--
--   file.upload-dir=C:/Users/SDS/Desktop/sw/nubi/nubi/backend/uploads
-- ============================================================
