package de.achimonline.hueciq;

import de.achimonline.hueciq.ServiceListener;

interface ServiceAPI
{
      String getLatestAction();

      void addListener(ServiceListener listener);
      void removeListener(ServiceListener listener);
}
