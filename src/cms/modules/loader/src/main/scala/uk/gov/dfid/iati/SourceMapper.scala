package uk.gov.dfid.iati

trait SourceMapper[TSource, TTarget] {
  def map(source: TSource) : TTarget
}
