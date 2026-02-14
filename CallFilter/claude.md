# CallShield — Bloqueador de Llamadas KMP

## Qué es este proyecto

Aplicación móvil Kotlin Multiplatform (KMP) que rechaza automáticamente llamadas entrantes según reglas configurables. Funciona 100% offline, sin cuenta, sin acceso a contactos, sin internet.

- **Android**: target principal y funcional. Implementa bloqueo real vía `CallScreeningService`.
- **iOS**: target secundario / placeholder. Solo UI y navegación compartida. Sin bloqueo real (futuro con CallKit).

## Principios inviolables

1. **Offline first** — no hay llamadas de red, ni en el MVP ni implícitas.
2. **Sin cuenta** — no existe registro, login ni autenticación.
3. **Sin acceso a contactos** — nunca se solicita ni usa el permiso de contactos.
4. **Sin historial de llamadas** — la app no almacena registros de llamadas procesadas.
5. **Sin base de datos remota** — toda la persistencia es local.
6. **Toda decisión es local** — el motor de reglas vive en el módulo shared KMP.
7. **La app no identifica quién llama** — solo decide permitir o rechazar.

## Arquitectura

```
shared (KMP)
├── model/          → Modelos de datos (Rule, BlockedNumber, RuleResult, SystemState)
├── engine/         → Motor de reglas (evaluación, combinación de reglas)
├── repository/     → Persistencia local (reglas, lista bloqueada, preferencias)
└── config/         → Configuración y preferencias del usuario

androidApp
├── service/        → CallScreeningService (consumo del motor de reglas)
├── ui/             → Pantallas Compose (Onboarding, Home, Rules, BlockedList)
├── notification/   → Notificaciones de llamadas bloqueadas
└── system/         → Verificación de estado del sistema (rol de filtrado, batería)

iosApp
├── ui/             → Pantallas SwiftUI (placeholder, misma navegación)
└── state/          → Consumo del estado compartido desde KMP
```

## Módulo shared — Responsabilidades exactas

- **Motor de reglas**: recibe un número telefónico → devuelve permitir o rechazar.
- **Reglas MVP**:
  1. Bloquear números de 8 dígitos.
  2. Bloquear números presentes en la lista local.
  3. Bloquear números que coincidan con un patrón regex configurable.
  4. Bloquear todas las llamadas.
  5. Notificación opcional cuando se bloquea (flag de preferencia).
- **Evaluación**: si cualquier regla activa coincide → rechazar. Si ninguna → permitir.
- **Persistencia**: reglas activas/inactivas, lista bloqueada, preferencias. Todo local.

## Módulo Android — Responsabilidades exactas

- `CallScreeningService`: recibe llamada entrante, consulta al motor de reglas del shared, ejecuta la respuesta del sistema.
- Onboarding: guía para habilitar el rol de filtrado de llamadas del sistema.
- Verificación de estado: rol habilitado, optimización de batería, configuración completa.
- Notificaciones: aviso cuando una llamada es bloqueada (si la preferencia está activa).

## Módulo iOS — Responsabilidades exactas

- Navegación y renderizado de pantallas compartidas.
- Consumo del estado compartido desde KMP.
- **No implementa bloqueo real**. Es placeholder para futura integración con CallKit/Call Directory.
- No debe romper la compilación del shared.

## Pantallas

| Pantalla | Propósito |
|---|---|
| Onboarding | Guía para habilitar filtrado en Android |
| Protección (Home) | Estado del sistema: activa/inactiva, switch maestro, diagnóstico |
| Reglas | Configuración del motor (activar/desactivar reglas, editar regex) |
| Lista bloqueada | CRUD manual de números |

## Estados del sistema

1. **No configurada** — el usuario no habilitó el rol de filtrado.
2. **Configuración incompleta** — hay impedimentos (ej. optimización de batería).
3. **Protección activa** — el filtrado está operativo.

El Home comunica exclusivamente este estado.

## Qué NO hacer

- No agregar historial de llamadas.
- No agregar identificación de spam online.
- No agregar sincronización en la nube.
- No agregar estadísticas.
- No agregar bloqueo basado en contactos.
- No solicitar permisos de contactos, internet o almacenamiento externo.
- No crear endpoints, APIs ni servicios remotos.

## Convenciones de código

- **Lenguaje**: Kotlin para shared y Android. Swift solo para iosApp.
- **UI Android**: Jetpack Compose.
- **UI iOS**: SwiftUI (placeholder mínimo).
- **Arquitectura**: MVI en shared. Estado observable por las plataformas.
- **Persistencia**: DataStore o equivalente KMP. No Room en shared.
- **Testing**: tests unitarios del motor de reglas en commonTest.
- **Naming**: español para documentación, inglés para código.
