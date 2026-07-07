# 아키텍처

이 프로젝트는 로컬 환경에서 오디오 파일을 업로드하고, Python AI Worker가 STT를 수행한 뒤, Spring Boot backend가 Ollama로 후처리하여 가사 초안 JSON을 반환하는 학습용 구조입니다.

## 처리 흐름

```text
사용자
→ Spring Boot backend
→ Python ai-worker
→ faster-whisper
→ Spring Boot backend
→ Ollama
→ JSON 응답
```

## 구성 요소

- Spring Boot backend: 파일 업로드 API, ai-worker 호출, Ollama 후처리 흐름을 담당합니다.
- Python ai-worker: FastAPI 서버로 동작하며, 추후 faster-whisper 전사를 담당합니다.
- faster-whisper: 실제 STT 모델입니다. 현재는 TODO로 남겨져 있습니다.
- Ollama: 로컬 LLM 후처리용입니다. STT 결과를 가사 초안 형태로 정리할 예정입니다.

## 현재 범위

현재 단계에서는 실행 가능한 프로젝트 뼈대와 TODO 중심의 학습용 메서드만 제공합니다. 실제 음성 인식, 보컬 분리, YouTube URL 처리는 구현하지 않았습니다.
