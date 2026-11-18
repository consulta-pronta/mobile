
import { setGlobalOptions } from "firebase-functions";
import { onDocumentCreated } from "firebase-functions/v2/firestore";

import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

setGlobalOptions({ maxInstances: 10 });

initializeApp();

const db = getFirestore();

export const sendNotification = onDocumentCreated(
  "users/{userId}/notifications/{notificationId}",
  async (event) => {
    const snapshot = event.data;

    if (!snapshot) {
      console.log("Documento não encontrado.");
      return;
    }

    const notification = snapshot.data();

    const userId = event.params.userId;
    const notificationId = event.params.notificationId;

    const title = notification.title || "ConsultaPronta";
    const description =
      notification.description || "Você possui uma nova notificação.";

    // Busca os tokens FCM do usuário
    const tokensSnapshot = await db
      .collection("users")
      .doc(userId)
      .collection("fcmTokens")
      .get();

    const tokens = tokensSnapshot.docs
      .map((doc) => doc.data().token || doc.id)
      .filter((token): token is string => typeof token === "string" && token.length > 0);

    if (tokens.length === 0) {
      console.log("Nenhum dispositivo registrado para este usuário.");
      return;
    }

    // Envia a notificação para os dispositivos
    const response = await getMessaging().sendEachForMulticast({
      tokens,
      notification: {
        title,
        body: description,
      },
      data: {
        notificationId,
        title,
        description,
      },
    });

    console.log(
      `Notificações enviadas: ${response.successCount}`,
    );

    console.log(
      `Falhas no envio: ${response.failureCount}`,
    );
  }
);
