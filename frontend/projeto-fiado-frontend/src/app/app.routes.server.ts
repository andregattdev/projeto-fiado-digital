import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Rotas renderizadas apenas no navegador.
 * Pré-render (RenderMode.Prerender) gerava HTML com spinner travado,
 * pois a API (localhost:8080) não existe no momento do build/SSR.
 */
export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
