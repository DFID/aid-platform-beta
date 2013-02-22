package controllers.admin

import play.api.mvc.Controller
import models.WhatWeAchieveEntry
import controllers.traits.admin.Secured
import com.google.inject.Inject
import uk.gov.dfid.common.api.Api

import concurrent.ExecutionContext.Implicits.global


class WhatWeAchieve @Inject()(val api: Api[WhatWeAchieveEntry]) extends Controller with Secured {

def index = SecuredAction { user => request =>
  Async {
    api.all.map { entries =>
    Ok(views.html.admin.whatweachieve.index(entries))
    }
  }
}

def create = SecuredAction { user => request =>
  Ok(views.html.admin.whatweachieve.view(None, WhatWeAchieveEntry.form))
}

def save = SecuredAction { user => implicit request =>
  WhatWeAchieveEntry.form.bindFromRequest.fold(
    errors => BadRequest(views.html.admin.whatweachieve.view(None, errors)),
    entry => {
      api.insert(entry)
      Redirect(routes.WhatWeAchieve.index())
    }
  )
}

def edit(id: String) = SecuredAction { user => implicit request =>
  Async {
    api.get(id).map { maybeWhatWeAchieve =>
      maybeWhatWeAchieve.map { whatweachieve =>
        val form = WhatWeAchieveEntry.form.fill(whatweachieve)
          Ok(views.html.admin.whatweachieve.view(Some(id), form))
        } getOrElse {
          Redirect(routes.WhatWeAchieve.index())
        }
    }
  }
}

def update(id: String) = SecuredAction { user => implicit request =>
  WhatWeAchieveEntry.form.bindFromRequest.fold(
    errors => BadRequest(views.html.admin.whatweachieve.view(Some(id), errors)),
    entry => {
      api.update(id, entry)
      Redirect(routes.WhatWeAchieve.index)
    }
)
}

def delete(id: String) = SecuredAction { user => implicit request =>
api.delete(id)
Redirect(routes.WhatWeAchieve.index)
}
}
