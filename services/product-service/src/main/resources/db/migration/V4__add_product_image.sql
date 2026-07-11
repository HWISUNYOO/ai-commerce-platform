-- 상품 이미지: 데모에서는 카테고리별 이모지를 이미지로 사용한다.
-- (운영에서는 image_url을 두고 실제 이미지는 오브젝트 스토리지(S3/Azure Blob)에 저장, URL만 DB에 보관하는 것이 정석.)
ALTER TABLE products ADD COLUMN image_emoji VARCHAR(16);

UPDATE products SET image_emoji = '⌨️'  WHERE name IN ('기계식 키보드', '무선 기계식 키보드');
UPDATE products SET image_emoji = '🖱️'  WHERE name IN ('무선 게이밍 마우스', '인체공학 버티컬 마우스');
UPDATE products SET image_emoji = '🖥️'  WHERE name IN ('27인치 게이밍 모니터', '34인치 울트라와이드 모니터', '듀얼 모니터암');
UPDATE products SET image_emoji = '🎧'  WHERE name IN ('노이즈캔슬링 헤드폰', '무선 이어버드');
UPDATE products SET image_emoji = '📷'  WHERE name IN ('1080p 웹캠', '4K 웹캠');
UPDATE products SET image_emoji = '🔌'  WHERE name IN ('USB-C 멀티 허브', '65W 고속충전기');
UPDATE products SET image_emoji = '💻'  WHERE name IN ('14인치 노트북', '게이밍 노트북');
UPDATE products SET image_emoji = '🎙️'  WHERE name = 'USB 콘덴서 마이크';
UPDATE products SET image_emoji = '🔋'  WHERE name = '20000mAh 보조배터리';
UPDATE products SET image_emoji = '⌚'  WHERE name = '스마트워치';
UPDATE products SET image_emoji = '🤖'  WHERE name = '로봇청소기';
UPDATE products SET image_emoji = '💨'  WHERE name = '공기청정기';
UPDATE products SET image_emoji = '🧹'  WHERE name = '무선 핸디청소기';
UPDATE products SET image_emoji = '🪥'  WHERE name = '전동칫솔';
UPDATE products SET image_emoji = '🪑'  WHERE name IN ('인체공학 사무용 의자', '전동 높이조절 데스크');
UPDATE products SET image_emoji = '📐'  WHERE name = '대형 데스크 매트';
UPDATE products SET image_emoji = '💡'  WHERE name = 'LED 모니터 스탠드바';

-- 매핑되지 않은 상품은 기본 아이콘
UPDATE products SET image_emoji = '📦' WHERE image_emoji IS NULL;
