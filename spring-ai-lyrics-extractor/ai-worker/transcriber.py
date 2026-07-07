from pathlib import Path
from faster_whisper import WhisperModel

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
        beam_size=5,
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