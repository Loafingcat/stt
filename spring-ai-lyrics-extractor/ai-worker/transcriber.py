from pathlib import Path
from faster_whisper import WhisperModel

"""
faster-whisper 모델을 전역으로 한 번만 로딩합니다.

왜 함수 안에서 로딩하지 않냐?
- 함수 안에서 모델을 로딩하면 요청이 올 때마다 모델을 새로 불러옵니다.
- 그러면 매 요청마다 매우 느려집니다.
- 서버 시작 시 한 번만 로딩하고, 요청 때는 이미 로딩된 model을 재사용하는 게 좋습니다.

처음에는 small 모델을 씁니다.
- large-v3는 더 정확하지만 무겁습니다.
- 지금은 프로젝트 연결 성공이 먼저라서 small이 적당합니다.
"""
model = WhisperModel(
    "small",
    device="cpu",
    compute_type="int8"
)


def transcribe_audio(file_path: Path) -> dict:
    """
    faster-whisper를 사용해 오디오 파일을 전사합니다.

    반환 형태는 Spring Boot의 RawTranscriptionResponse record와 맞춥니다.

    Java 쪽 DTO:
    RawTranscriptionResponse(
        String filename,
        String language,
        List<LyricSegment> segments
    )

    Python 반환 JSON:
    {
        "filename": "...",
        "language": "ko",
        "segments": [
            {
                "start": 0.0,
                "end": 5.0,
                "text": "전사된 문장"
            }
        ]
    }
    """

    segments, info = model.transcribe(
        str(file_path),

        # beam_size가 높으면 더 신중하게 탐색하지만 느려질 수 있습니다.
        # 일단 5 정도로 둡니다.
        beam_size=5,

        # vad_filter는 무음 구간을 제거하는 옵션입니다.
        # 노래에서는 보컬이 약하거나 반주가 있으면 잘릴 수 있으므로 일단 False로 둡니다.
        vad_filter=False
    )

    result_segments = []

    for segment in segments:
        text = segment.text.strip()

        # 완전히 빈 텍스트는 응답에서 제외합니다.
        if not text:
            continue

        result_segments.append({
            "start": float(segment.start),
            "end": float(segment.end),
            "text": text
        })

    return {
        "filename": file_path.name,
        "language": info.language,
        "segments": result_segments
    }