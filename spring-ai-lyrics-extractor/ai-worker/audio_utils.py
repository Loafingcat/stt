from pathlib import Path
from fastapi import UploadFile, HTTPException
import shutil
import uuid
import ffmpeg

# 업로드된 오디오 파일을 임시 저장할 폴더입니다.
TEMP_DIR = Path("temp")

# 처음 프로젝트에서는 mp3, wav, m4a만 허용합니다.
# flac, ogg 등은 나중에 확장해도 됩니다.
ALLOWED_EXTENSIONS = {".mp3", ".wav", ".m4a"}


def validate_audio_file(file: UploadFile) -> None:
    """
    업로드된 파일이 우리가 허용한 오디오 파일인지 검사합니다.

    여기서는 확장자 기준으로 검사합니다.

    왜 MIME 타입이 아니라 확장자를 보냐?
    - m4a 파일은 환경에 따라 audio/mp4, audio/x-m4a, application/octet-stream 등으로 들어올 수 있습니다.
    - 초반 개발 단계에서는 MIME 타입만 믿으면 정상 파일도 막힐 수 있습니다.
    - 그래서 지금은 확장자 기준으로 단순하게 검증합니다.
    """

    filename = file.filename

    if not filename:
        raise HTTPException(status_code=400, detail="파일 이름이 없습니다.")

    suffix = Path(filename).suffix.lower()

    if suffix not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=400,
            detail=f"지원하지 않는 파일 형식입니다: {suffix}. mp3, wav, m4a만 지원합니다."
        )


def save_upload_file(file: UploadFile) -> Path:
    """
    FastAPI로 받은 UploadFile을 temp 폴더에 저장합니다.

    왜 저장하냐?
    - faster-whisper는 파일 경로를 넘겨서 처리하는 방식이 편합니다.
    - ffmpeg도 파일 경로 기반으로 변환하는 게 안정적입니다.
    - 그래서 업로드된 파일을 일단 temp 폴더에 저장합니다.
    """

    TEMP_DIR.mkdir(parents=True, exist_ok=True)

    original_suffix = Path(file.filename).suffix.lower()

    # 같은 파일명이 들어와도 충돌하지 않도록 uuid를 붙입니다.
    saved_filename = f"{uuid.uuid4()}{original_suffix}"
    saved_path = TEMP_DIR / saved_filename

    with saved_path.open("wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    return saved_path


def convert_to_wav(input_path: Path) -> Path:
    """
    입력 오디오 파일을 Whisper가 처리하기 좋은 wav 파일로 변환합니다.

    변환 기준:
    - ar=16000: 샘플레이트 16kHz
    - ac=1: 모노 채널

    왜 16kHz mono로 바꾸냐?
    - 음성 인식 모델은 보통 고품질 스테레오 음원이 필요하지 않습니다.
    - 파일 크기를 줄이고 처리 속도를 높이기 위해 mono로 바꿉니다.
    - 16kHz는 음성 인식에서 자주 쓰는 샘플레이트입니다.
    """

    # 이미 wav여도 통일된 형식으로 다시 변환합니다.
    output_path = input_path.with_suffix(".wav")

    try:
        (
            ffmpeg
            .input(str(input_path))
            .output(
                str(output_path),
                ar=16000,
                ac=1,
                format="wav"
            )
            .overwrite_output()
            .run(quiet=True)
        )

        return output_path

    except ffmpeg.Error as e:
        raise HTTPException(
            status_code=500,
            detail=f"ffmpeg 변환 중 오류가 발생했습니다: {e}"
        )