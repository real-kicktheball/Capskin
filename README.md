🌈 CapSkin: Deep Learning-based Spectral Reconstruction & Skin Analysis
RGB를 넘어 분광으로.
스마트폰 카메라와 딥러닝을 결합하여 고가의 장비 없이 피부 스펙트럼을 복원하고, 정밀한 피부 성분 분석 및 뷰티 솔루션을 제공하는 차세대 스펙트럼 분석 파이프라인입니다.

📌 Project Overview
기존의 RGB 이미지는 단 3채널의 색상 정보만을 담고 있어, 서로 다른 파장이 같은 색으로 보이는 메타메리즘(Metamerism) 현상을 구분하거나 피부 하부의 성분(멜라닌, 헤모글로빈 등)을 분리하는 데 한계가 있습니다.

CapSkin은 이러한 한계를 극복하기 위해 딥러닝 기반 스펙트럼 복원 기술을 활용합니다. 스마트폰 촬영 데이터로부터 31개 밴드의 분광 반사율(Spectral Reflectance)을 추정하여 전문 분광 장비 수준의 정밀 진단 서비스를 대중화하는 것을 목표로 합니다.

🚀 Key Features
1. 딥러닝 기반 스펙트럼 복원
MST++ & SSTHyper: 최신 SOTA 모델을 기반으로 RGB 데이터를 31개 밴드의 분광 데이터로 정밀 복원합니다.

Calibration System: 저가형 캘리브레이션 카드를 활용해 다양한 조명 환경에서의 색 왜곡을 보정, 데이터 신뢰도를 확보합니다.

2. 🌟 차별화된 스킨케어 기능
피부 고민 원인 레이어 지도: 멜라닌(색소형)과 헤모글로빈(혈관형) 농도를 히트맵으로 얼굴 위에 오버레이하여 시각화합니다.

조명 변환 AR 뷰어: 카페, 야외, 사무실 등 다양한 조명 환경에서 화장품 발색이 어떻게 변하는지 실시간 시뮬레이션을 제공합니다.

성분 충돌 감지기: 사용자의 피부 스펙트럼 특성과 맞지 않는 화장품 성분 조합을 분석하여 자극 위험을 경고합니다.

퍼스널 컬러 스펙트럼 패스포트: 주관적 판단이 아닌 반사율 수치에 근거한 객관적 컬러 타입 인증서를 발급합니다.

3. 스마트 루틴 & 가이드
맞춤형 처방전: 분석된 멜라닌/헤모글로빈 수치를 바탕으로 아침/저녁 맞춤형 루틴 및 피해야 할 성분 가이드를 자동 생성합니다.

3조명 테스트 매칭: 어떤 조명에서도 피부톤과 들뜨지 않는(2ΔE 이내) 최적의 파운데이션을 추천합니다.

🛠️ Tech Stack
Mobile & Frontend
Android SDK (Kotlin / Jetpack Compose)

CameraX: 실시간 카메라 스트리밍 및 이미지 캡처

Google ML Kit: 얼굴 인식 및 ROI(관심 영역) 추출

Deep Learning & Backend
PyTorch / TensorFlow: 스펙트럼 복원 모델 구현 및 추론

MST++ (Multi-stage Spectral Transformer): 베이스라인 모델

GPT API: 개인 맞춤형 피부 관리 가이드 자연어 생성

Dataset
NTIRE 2022 Spectral Reconstruction Challenge Data

Hyper-Skin Open Dataset

📖 Analysis Pipeline
Input: 스마트폰으로 캘리브레이션 카드와 함께 얼굴 촬영

Preprocessing: 조명 왜곡 보정 및 얼굴 부위별(볼, 이마 등) ROI 데이터 추출

Reconstruction: 딥러닝 모델을 통한 픽셀별 31밴드 반사율 스펙트럼 복원

Analysis: 멜라닌/헤모글로빈 피크 분석 및 ITA값 계산을 통한 피부 타입 분류

Output: 얼굴 히트맵 리포트, 맞춤형 제품 추천 및 루틴 가이드 제공

📈 Technical Validity
최신 기술 트렌드: NTIRE 2022 챌린지의 우승 모델인 MST++ 등을 활용하여 기술적 성숙도를 확보했습니다.

지속적 성능 향상: 2024-2025년 최신 연구(SSTHyper, MSS-Mamba 등)에서 MRAE 지표가 지속적으로 개선됨에 따라 캡스톤 프로젝트로서의 타당성을 입증했습니다.

의료/산업 확장성: 분광 분석 기술이 수술 영상 분석, 의료, 식품 분야로 확대됨에 따라 뷰티 분야 적용의 타당성을 뒷받침합니다.


https://github.com/user-attachments/assets/0ba39103-081a-48f8-b460-4254fa087cf9


👤 Author
Name: chanhu (University Student, 4th Year)

Major: Information Security / Information and Communication Engineering

University: Chosun University

Contact: GitHub Profile

This project is part of the 2026 Capstone Design.
