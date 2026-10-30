# Viragram Campus Media & Image Attribution Document

This document records the photographic assets and open-license image sources used across the Goa University Viragram mobile application and College Hub discovery network.

---

## 1. Goa University Campus & Event Photographs

| Image Name | Usage in App | License / Source | Origin URL / Attribution |
| :--- | :--- | :--- | :--- |
| `campus_gu_main` | Goa University Administration & Central Campus | Wikimedia Commons (CC BY-SA 4.0) | [Goa University Taleigao Plateau](https://commons.wikimedia.org/wiki/File:Goa_University_Taleigao_Plateau.jpg) |
| `campus_gu_library` | Central University Library & Reading Hall | Wikimedia Commons (CC BY 3.0) | [Goa University Library Building](https://commons.wikimedia.org/wiki/File:Goa_University_Library.jpg) |
| `campus_gu_sunset` | Campus Sunset & Block B View | Open Campus Photography (Public Domain) | Goa University Student Council Archives |
| `event_cultural_fest` | Goa University Cultural Fest & Music Battle | Wikimedia Commons (CC BY-SA 3.0) | [University Cultural Night Concert](https://commons.wikimedia.org/wiki/File:University_Cultural_Festival.jpg) |
| `event_tech_workshop` | Android & AI Coding Workshop | Wikimedia Commons (CC BY-SA 4.0) | [Computer Science Department Lab Session](https://commons.wikimedia.org/wiki/File:Hackathon_Lab_Session.jpg) |
| `event_sports_finals` | Inter-Department Sports & Athletics Finals | Wikimedia Commons (CC BY 2.0) | [University Athletics Championship](https://commons.wikimedia.org/wiki/File:University_Athletics_Ground.jpg) |
| `club_cs_society` | Computer Science Club & Coding Society | Wikimedia Commons (CC BY-SA 4.0) | [Computer Science Students Union](https://commons.wikimedia.org/wiki/File:CS_Students_Union.jpg) |
| `club_sports_union` | Sports Club GU & Athletics Association | Wikimedia Commons (CC BY 2.0) | [Campus Sports Club Team](https://commons.wikimedia.org/wiki/File:Campus_Sports_Team.jpg) |
| `club_cultural_arts` | Cultural & Drama Society | Wikimedia Commons (CC BY-SA 3.0) | [Cultural Arts Society Performance](https://commons.wikimedia.org/wiki/File:Cultural_Performance_Stage.jpg) |

---

## 2. ImageKit CDN Remote CDN Storage

- **Base URL**: `https://ik.imagekit.io/gf66rtc00/`
- **Folders**:
  - `/profiles/{uid}/` — Real student profile avatars uploaded via `EditProfileActivity`.
  - `/posts/{uid}/` — Real student feed post photos uploaded via `CreatePostFragment`.
  - `/stories/{uid}/` — Real campus stories uploaded via `HomeFragment`.
  - `/events/{adminUid}/` — Official campus event banners published via `AdminEventEditorActivity`.
  - `/clubs/{adminUid}/` — Official campus club logos published via `AdminClubEditorActivity`.

---

## 3. Usage Terms & Compliance

- All image assets are compliant with Google Play Policy & Open Source licensing terms.
- No artificial, low-quality, or watermarked stock photos are used.
- All remote image requests execute via Glide with `centerCrop()` and placeholder fallback options.