@echo off
chcp 936 > nul 2>&1  :: ǿ������cmd����ΪGBK���������ģ�
echo ========================================
echo �칫����ֲ����ϵͳ - �����ű�
echo ========================================
echo.

:: ��1/3��������˷�������·��У�飩
echo [1/3] ������˷���...
if exist "plant-adoption-server" (
    cd /d plant-adoption-server
    start "��˷���" cmd /k "mvn spring-boot:run"
    cd /d ..
) else (
    echo ����δ�ҵ����Ŀ¼ plant-adoption-server
    pause
    exit /b 1
)
timeout /t 10 /nobreak > nul

:: ��2/3������H5ǰ�ˣ�����·��У�飩
echo [2/3] ����H5ǰ��...
if exist "plant-adoption-h5" (
    cd /d plant-adoption-h5
    start "H5ǰ��" cmd /k "pnpm dev:h5"
    cd /d ..
) else (
    echo ����δ�ҵ�H5Ŀ¼ plant-adoption-h5
    pause
    exit /b 1
)
timeout /t 5 /nobreak > nul

:: ��3/3������������̨������·��У�飩
echo [3/3] ����������̨...
if exist "plant-adoption-admin" (
    cd /d plant-adoption-admin
    start "������̨" cmd /k "pnpm dev"
    cd /d ..
) else (
    echo ����δ�ҵ�������̨Ŀ¼ plant-adoption-admin
    pause
    exit /b 1
)

echo.
echo ========================================
echo ���з���������ɣ�
echo ========================================
echo.
echo ��˷���: http://localhost:8080
echo H5ǰ��:   http://localhost:3000
echo ������̨: http://localhost:3001
echo API�ĵ�:  http://localhost:8080/api/doc.html
echo.
pause