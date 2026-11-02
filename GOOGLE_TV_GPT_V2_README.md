# Google TV GPT V2 — osobny launcher (alpha)

Ta aplikacja jest **odrębnym instalowalnym pakietem Android TV**, nie aktualizacją KS Launcher.

- applicationId: `pl.ksawery.googletvgptv2`
- Etykieta: **Google TV GPT V2**
- Domyślny wygląd: motyw Google TV (Theme4).
- Animacje fokusu: Off / Google TV / Apple TV / Fade, wybierane w Ustawieniach.
- Po HOME: prośba o fokus na pierwszą ulubioną aplikację.
- Przy powrocie z innej aplikacji: odświeżanie listy aplikacji tylko z ograniczoną częstotliwością, ikony w cache, ograniczony rozmiar plakatów i media polling tylko na pierwszym planie.
- 1080p/60 Hz docelowy test na S905X4/2GB RAM i telewizorze Full HD.

**Pochodzenie:** projekt odrębny przez package ID i dane; część kodu jest technicznie ponownie wykorzystana z wcześniejszego KS, ponieważ jego UI użytkownik preferował. To NIE jest od podstaw przepisany silnik launchera ani ukończona kopia Google TV 1:1.

**Ograniczenia:** zgodność z Android TV 14, 60 FPS, czas uruchamiania usług i usunięcie crashy nie zostały sprawdzone na fizycznym TV Boxie. Netflix nie udostępnia pełnego Watch Next standardowym interfejsem Android TV.

**Bezpieczeństwo:** nie wyłączać fabrycznego launchera. Nie odinstalowywać KS. Nowy package ID pozwala instalować równolegle. W razie problemów wróć do ustawień systemowych i wybierz poprzedni Home.

**Repozytorium:** na osobnej gałęzi `google-tv-gpt-v2-standalone-20261102`; istniejące gałęzie pozostają bez zmian.
