# AgriHealth Alert
*(Veterinary Telemedicine and Rapid Disease Reporting for Farmers)*

## Pitch
Farmers often face uncertainty when animals show unusual symptoms. Calling a veterinarian for every case can be costly and time-consuming, while delays in reporting contagious diseases can worsen outbreaks. **AgriHealth Alert** combines veterinary telemedicine with rapid outbreak response.

Farmers can quickly upload photos and a short description of symptoms and receive feedback from veterinary professionals. In normal use, this supports affordable, fast veterinary guidance. In the event of a suspected highly contagious animal disease outbreak, veterinary professionals can escalate reports to competent authorities, enabling rapid situational awareness, resource allocation, and containment measures. The app provides a professional, structured communication channel that strengthens both daily herd health management and emergency disease surveillance.

---

## Features
-  **Photo upload with GPS coordinates** for precise reporting  
-  **Short symptoms report** (number of animals, duration, severity, optional notes)  
-  **Veterinary feedback loop**: professionals review and respond to farmer submissions  
-  **Crisis escalation**: urgent cases flagged to authorities for investigation and containment  
-  **Map visualization** of reported cases (only for canton veterinary)  
-  **Privacy management**: farmers choose whether reports are public; authorities retain full oversight  
-  **Offline mode**: draft reports without Internet; auto-send when back online  

---

## Requirements
- **Split App Model**: Local storage for reports and offline functionality; profiles, history, and outbreak data stored in Firebase  
- **User Support**: Three roles
  - Farmers (submit reports, receive vet advice)  
  - Veterinary professionals (review regional cases, escalate)  
  - Veterinary authorities (monitor reports, manage crisis data)  
- **Sensor Use**: Camera for photos, GPS for outbreak localization  
- **Offline Mode**: Farmers and vets can create/review reports offline, with syncing when online  

---

## Usage Examples
-  A pack of wolves attacks a farmer’s sheep. The farmer posts an alert to inform nearby farmers and simultaneously requests veterinary assistance. The veterinarian advises on immediate care and escalates if necessary.  
-  A farmer notices unusual skin lesions on cows. They send a private report to a nearby vet, who may escalate to authorities if a contagious disease is suspected.  
-  During routine checks, a farmer notices chickens with respiratory distress. They upload photos + a brief report; vets provide advice while monitoring outbreak patterns.  

---

## Further Improvements
-  Introduce a **guest mode** for the general public, allowing limited viewing of authorized reports.  
-  Allow the public to submit relevant observations (e.g., dead wild animals) to help monitoring.  
-  Implement **push notifications** for nearby farmers when contagious diseases are reported.  
-  Integrate **automated alerts & analytics** for vets/authorities to detect clusters of unusual symptoms.  
