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

    TEMP_DIR.mkdir(parents=True, exist_ok=True)

    original_suffix = Path(file.filename).suffix.lower()

    # 같은 파일명이 들어와도 충돌하지 않도록 uuid를 붙입니다.
    saved_filename = f"{uuid.uuid4()}{original_suffix}"
    saved_path = TEMP_DIR / saved_filename

    with saved_path.open("wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    return saved_path


def convert_to_wav(input_path: Path) -> Path:

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