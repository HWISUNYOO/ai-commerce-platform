-- 데모용 샘플 상품. 프론트엔드 상품 목록이 비어있지 않도록 초기 데이터를 넣는다.
INSERT INTO products (name, description, price, stock_quantity, status, created_at, updated_at) VALUES
('기계식 키보드',        '텐키리스 청축, 백라이트',        89000,  50,  'ACTIVE', now(), now()),
('무선 게이밍 마우스',    '초경량 55g, 무선 2.4GHz',        39000,  120, 'ACTIVE', now(), now()),
('27인치 게이밍 모니터',  'QHD 165Hz, HDR',                320000, 30,  'ACTIVE', now(), now()),
('USB-C 멀티 허브',      '7-in-1, HDMI/USB/SD',            45000,  200, 'ACTIVE', now(), now()),
('노이즈캔슬링 헤드폰',   '블루투스 5.3, 30시간 재생',       210000, 40,  'ACTIVE', now(), now()),
('1080p 웹캠',          '60fps, 자동 초점',               68000,  80,  'ACTIVE', now(), now());
