from fastapi import FastAPI, UploadFile, File
from audio_utils import validate_audio_file, save_upload_file, convert_to_wav
from transcriber import transcribe_audio

app = FastAPI(title="Lyrics AI Worker")


@app.get("/health")
def health_check():
    """
    ai-worker 서버가 살아있는지 확인하는 API입니다.
    """
    return {
        "status": "ok"
    }


@app.post("/transcribe")
def transcribe(file: UploadFile = File(...)):
    """
    Spring Boot backend에서 오디오 파일을 보내면
    이 API가 파일을 저장하고 전사 결과를 반환합니다.
    """

    # 1. 파일 형식 검증
    validate_audio_file(file)

    # 2. 업로드 파일 저장
    saved_path = save_upload_file(file)

    # 3. wav 변환
    wav_path = convert_to_wav(saved_path)

    # 4. STT 전사
    result = transcribe_audio(wav_path)

    # 5. 결과 반환
    return result