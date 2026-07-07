# AI Worker

Python FastAPI 기반 STT 작업자 뼈대입니다.

현재는 `/transcribe` 엔드포인트, 파일 저장 함수, 전사 함수 이름만 준비되어 있습니다. 실제 faster-whisper 모델 로딩과 전사 로직은 TODO로 남겨 두었습니다.

## 실행

```bash
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn app:app --reload --port 8000
```

## v2에서 추가 예정

- faster-whisper 실제 전사 구현
- Demucs 기반 보컬 분리
- 타임스탬프 세그먼트 반환
