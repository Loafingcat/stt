# Spring AI Day4 연결 정리

이 문서는 발표나 질의응답을 준비하기 위한 학습 정리입니다. GitHub 첫 화면용 README에는 핵심 실행 정보만 남기고, 수업과 프로젝트의 연결 분석은 이 문서에 분리합니다.

## 전체 평가

`spring-ai-lyrics-extractor`는 Spring AI Day4 Multimodal 수업의 파일 업로드 흐름과 직접 연결됩니다. 다만 수업 예제를 그대로 복사한 프로젝트는 아니며, 오디오 멀티모달 입력 흐름을 가사 초안 추출이라는 주제로 변형했습니다.

직접 연결되는 부분:

- `multipart/form-data`
- `MultipartFile`
- `ByteArrayResource`
- 파일 타입 검증
- 파일 크기 제한
- Java `record` DTO
- 구조화된 JSON 응답

아직 직접 반영되지 않은 부분:

- Spring AI `Media`
- `MimeType + Resource + Media` 패턴
- ChatClient `.media()` 호출
- `.entity(Record.class)` 기반 구조화 출력
- Advisor
- Chat Memory
- `conversationId`

## 왜 `.media()`로 직접 보내지 않았나

수업 예제는 오디오 파일을 LLM에 직접 전달하는 흐름을 다룹니다. 하지만 이 프로젝트는 노래 가사 추출이 목표이기 때문에 STT 전용 모델을 먼저 사용하는 구조가 더 현실적입니다.

이유:

- Ollama는 오디오 파일 자체를 안정적으로 전사하는 STT 모델이 아닙니다.
- 노래는 일반 음성보다 인식이 어렵습니다.
- 반주, 코러스, 리버브, 발음 뭉개짐이 STT 품질을 떨어뜨립니다.
- `faster-whisper`는 로컬에서 사용할 수 있는 STT 전용 모델입니다.
- `ffmpeg`, `faster-whisper`, Demucs 같은 오디오 AI 도구는 Python 생태계가 더 편합니다.

## 발표용 1분 설명

제가 만든 프로젝트는 `spring-ai-lyrics-extractor`입니다. 사용자가 mp3, wav, m4a 같은 오디오 파일을 업로드하면 Spring Boot backend가 `MultipartFile`로 파일을 받고, 이 파일을 다시 Python FastAPI worker에 multipart로 전달합니다. Python worker는 ffmpeg로 오디오를 wav 형식으로 변환한 뒤 faster-whisper로 음성을 텍스트로 전사합니다. 그 결과는 `RawTranscriptionResponse`, `LyricSegment`, `LyricsExtractResponse` 같은 record DTO 형태로 Spring Boot에 돌아옵니다.

오늘 배운 멀티모달 수업과 연결되는 부분은 파일 업로드, multipart/form-data, Resource 변환, 구조화 응답입니다. 다만 수업 예제를 그대로 베낀 건 아니고, 오디오 입력 흐름을 가사 초안 추출이라는 주제로 바꿨습니다. Ollama는 오디오를 직접 듣는 역할이 아니라, 전사된 텍스트를 가사 초안처럼 정리하는 후처리 역할로 사용할 예정입니다.

## 발표용 3분 설명

오늘 수업에서는 Spring AI의 멀티모달 입력을 배웠습니다. 핵심은 사용자가 이미지, PDF, 오디오 같은 파일을 `multipart/form-data`로 보내면, Spring MVC가 이것을 `MultipartFile`로 받고, 그 파일을 `ByteArrayResource` 같은 Resource로 변환해서 MimeType과 함께 LLM에 전달할 수 있다는 점이었습니다. 그리고 결과를 자유 텍스트가 아니라 Java record DTO로 구조화해서 받을 수 있다는 것도 중요한 내용이었습니다.

제가 만든 `spring-ai-lyrics-extractor`는 이 흐름을 바탕으로 만든 로컬 AI 프로젝트입니다. 사용자가 오디오 파일을 `/api/lyrics/extract`로 업로드하면 Spring Boot backend가 `MultipartFile`로 파일을 받습니다. 그리고 이 파일을 `ByteArrayResource`로 감싸서 Python FastAPI worker의 `/transcribe` API로 다시 multipart 전송합니다. Python worker에서는 파일을 저장하고, ffmpeg로 wav 형식으로 변환한 뒤, faster-whisper를 이용해서 STT를 수행합니다. 결과는 `filename`, `language`, `segments`를 가진 JSON으로 반환되고, Spring Boot에서는 이것을 `RawTranscriptionResponse`, `LyricSegment`, `LyricsExtractResponse` 같은 record DTO로 다룹니다.

수업 예제와 다른 점도 있습니다. 수업에서는 오디오 파일을 Spring AI의 `.media()`로 LLM에 직접 전달하는 흐름을 배웠지만, 제 프로젝트에서는 그렇게 하지 않았습니다. 이유는 Ollama가 오디오 파일 자체를 안정적으로 분석하는 STT 모델이 아니고, 특히 노래 가사는 일반 음성보다 훨씬 어렵기 때문입니다. 노래에는 반주, 코러스, 리버브, 발음 뭉개짐이 있어서 Whisper 계열 같은 STT 전용 모델이 더 적합합니다. 그래서 STT는 Python의 faster-whisper가 담당하고, Spring Boot는 API 흐름과 응답 구조를 담당하며, Ollama는 나중에 전사 결과를 가사 초안처럼 정리하는 후처리 역할로 붙일 예정입니다.

정리하면, 이 프로젝트는 오늘 배운 멀티모달 파일 입력 개념을 그대로 복사한 게 아니라, 오디오 파일 업로드와 구조화 응답이라는 핵심을 가져와서 가사 추출이라는 현실적인 주제로 확장한 프로젝트입니다.

## 예상 질문과 짧은 답변

### MultipartFile이 뭔가요?

Spring MVC에서 파일 업로드를 받을 때 쓰는 타입입니다. 사용자가 `multipart/form-data`로 보낸 파일을 Java 코드에서 파일명, 크기, content type, byte 데이터로 다룰 수 있게 해줍니다.

### multipart/form-data가 JSON 요청과 다른 점은 뭔가요?

JSON은 보통 텍스트 데이터 하나를 body에 담지만, `multipart/form-data`는 여러 part로 나눠서 파일과 텍스트 값을 같이 보낼 수 있습니다. 파일 업로드에는 JSON보다 multipart가 적합합니다.

### MimeType, Resource, Media는 각각 어떤 역할인가요?

MimeType은 파일 형식, Resource는 파일 데이터, Media는 이 둘을 묶어서 LLM에게 “이건 이런 타입의 파일이다”라고 전달하는 객체입니다.

### ByteArrayResource를 왜 쓰나요?

Spring에서 파일 내용을 Resource 형태로 다시 감싸기 위해 씁니다. 이 프로젝트에서는 Spring이 받은 `MultipartFile`을 Python worker에 다시 multipart로 전달할 때 사용했습니다.

### `.media()`를 빼먹으면 어떤 문제가 생기나요?

LLM에게 파일 자체가 전달되지 않습니다. 텍스트 프롬프트만 보내는 상태가 되어서 이미지, PDF, 오디오 분석을 할 수 없습니다.

### `.entity()`를 쓰는 이유는 뭔가요?

LLM 응답을 문자열로 직접 파싱하지 않고, Java record 같은 정해진 타입으로 바로 받기 위해서입니다. 구조화된 결과가 필요할 때 유리합니다.

### 왜 Ollama만으로 가사 추출을 하지 않았나요?

Ollama는 로컬 텍스트 LLM 후처리에 적합하지만, 오디오 파일 자체를 안정적으로 듣고 전사하는 용도는 아닙니다. 그래서 음성 인식은 faster-whisper가 맡고 Ollama는 정리 역할로 두었습니다.

### 왜 Python worker를 따로 뺐나요?

faster-whisper, ffmpeg, 나중의 Demucs 같은 오디오 AI 도구는 Python 생태계가 훨씬 편합니다. Spring Boot는 API 서버와 서비스 흐름을 맡고, Python은 AI 처리를 맡게 분리했습니다.

### Demucs를 추가하면 뭐가 좋아지나요?

Demucs로 보컬과 반주를 분리하면 STT 모델이 목소리에 더 집중할 수 있습니다. 그래서 가사 인식 품질이 좋아질 가능성이 있습니다.

### Chat Memory를 붙이면 이 프로젝트에서 무엇을 할 수 있나요?

같은 파일의 전사 결과를 기억해두고 “2절만 다시 정리해줘”, “불확실한 부분만 보여줘”처럼 이어서 대화할 수 있습니다.

### Advisor를 붙이면 무엇을 개선할 수 있나요?

LLM 호출 전후 로깅, 호출 횟수 카운트, 공통 시스템 프롬프트 적용, 메모리 연결 같은 공통 기능을 서비스 코드와 분리해서 관리할 수 있습니다.
