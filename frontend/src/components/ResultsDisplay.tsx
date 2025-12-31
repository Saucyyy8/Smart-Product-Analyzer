import { Star, ThumbsUp, ThumbsDown, Crown, ExternalLink, Sparkles, Zap } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Progress } from "@/components/ui/progress";
import { GlowingEffect } from "@/components/ui/glowing-effect";

interface Product {
  id: number;
  name: string;
  price: string;
  rating: number;
  pros: string[];
  cons: string[];
  score: number;
  isRecommended: boolean;
  verdict?: string;
  url?: string;
  imageUrl?: string;
}

interface ResultsDisplayProps {
  results: {
    query: string;
    products: Product[];
  };
}

const ResultsDisplay = ({ results }: ResultsDisplayProps) => {
  const recommendedProduct = results.products.find(p => p.isRecommended);
  const otherProducts = results.products.filter(p => !p.isRecommended);

  return (
    <section className="py-20 bg-background">
      <div className="container mx-auto px-4">

        {/* Header - Simplified */}
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold mb-2">
            Analysis Results
          </h2>
        </div>

        {/* Main Recommendation - Compact Design */}
        {recommendedProduct && (
          <div className="mb-12 max-w-5xl mx-auto">
            <h3 className="text-xl font-semibold mb-4 flex items-center gap-2">
              <Sparkles className="h-5 w-5 text-yellow-500" />
              Top Recommendation
            </h3>

            <div className="relative rounded-xl border-[0.75px] border-border p-1">
              <GlowingEffect
                spread={40}
                glow={true}
                disabled={false}
                proximity={64}
                inactiveZone={0.01}
                borderWidth={3}
              />
              <div className="relative bg-card rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-shadow duration-300">
                <div className="flex flex-col md:flex-row h-full">
                  {/* Image Section - Compact */}
                  <div className="w-full md:w-1/3 bg-white p-6 flex items-center justify-center border-b md:border-b-0 md:border-r border-border/50">
                    <div className="relative w-full aspect-square max-w-[220px]">
                      <img
                        src={recommendedProduct.imageUrl || "/placeholder.svg"}
                        alt={recommendedProduct.name}
                        className="object-contain w-full h-full hover:scale-105 transition-transform duration-300 mix-blend-multiply"
                        onError={(e) => {
                          (e.target as HTMLImageElement).src = "/placeholder.svg";
                        }}
                      />
                    </div>
                  </div>

                  {/* Content Section */}
                  <div className="p-6 flex flex-col justify-between w-full md:w-2/3">
                    <div>
                      <div className="flex justify-between items-start gap-4 mb-2">
                        <div>
                          <Badge variant="secondary" className="mb-2 bg-primary/10 text-primary hover:bg-primary/20 transition-colors">
                            Recommended
                          </Badge>
                          <h3 className="text-xl md:text-2xl font-bold leading-tight line-clamp-2" title={recommendedProduct.name}>
                            {recommendedProduct.name}
                          </h3>
                        </div>
                        <div className="flex flex-col items-end shrink-0">
                          <div className="flex items-center gap-2 bg-secondary/50 px-3 py-1.5 rounded-lg border border-border/50">
                            <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Match</span>
                            <span className="text-lg font-bold text-primary">{recommendedProduct.rating?.toFixed(1) || "N/A"}</span>
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-3 mb-6">
                        <span className="text-3xl font-bold text-primary">
                          {recommendedProduct.price || "N/A"}
                        </span>
                      </div>

                      <div className="mb-6">
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-xs font-medium text-muted-foreground">Match Score</span>
                          <span className="text-xs font-bold">{recommendedProduct.score.toFixed(0)}/100</span>
                        </div>
                        <Progress value={recommendedProduct.score} className="h-2" />
                      </div>

                      {/* AI Verdict - Compact */}
                      <div className="bg-muted/30 rounded-lg p-4 mb-6 border border-border/50">
                        <div className="flex items-center gap-2 mb-2 text-xs uppercase tracking-wider font-bold text-muted-foreground">
                          <Zap className="h-3.5 w-3.5 text-yellow-500 fill-yellow-500" /> AI Verdict
                        </div>
                        <p className="text-sm text-foreground/90 leading-relaxed">
                          {recommendedProduct.verdict}
                        </p>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">

                      {/* Pros Popover/List - Simplified for Compactness */}
                      <div className="space-y-2">
                        <div className="flex items-center gap-2 text-sm font-semibold text-green-600 dark:text-green-400">
                          <ThumbsUp className="h-4 w-4" /> Pros
                        </div>
                        <ul className="text-xs text-muted-foreground space-y-1">
                          {recommendedProduct.pros.slice(0, 2).map((p, i) => (
                            <li key={i} className="flex gap-2">
                              <span className="block w-1 h-1 mt-1.5 rounded-full bg-green-500 shrink-0" />
                              <span className="line-clamp-1">{p}</span>
                            </li>
                          ))}
                        </ul>
                      </div>

                      {/* Cons Popover/List */}
                      <div className="space-y-2">
                        <div className="flex items-center gap-2 text-sm font-semibold text-red-600 dark:text-red-400">
                          <ThumbsDown className="h-4 w-4" /> Cons
                        </div>
                        <ul className="text-xs text-muted-foreground space-y-1">
                          {recommendedProduct.cons.slice(0, 2).map((c, i) => (
                            <li key={i} className="flex gap-2">
                              <span className="block w-1 h-1 mt-1.5 rounded-full bg-red-500 shrink-0" />
                              <span className="line-clamp-1">{c}</span>
                            </li>
                          ))}
                        </ul>
                      </div>
                    </div>

                    <div className="mt-6 pt-4 border-t border-border/50">
                      <Button
                        className="w-full"
                        size="lg"
                        onClick={() => recommendedProduct.url && window.open(recommendedProduct.url, '_blank')}
                      >
                        <ExternalLink className="h-4 w-4 mr-2" /> View on Amazon
                      </Button>
                    </div>

                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Other Products */}
        {otherProducts.length > 0 && (
          <div>
            <h3 className="text-2xl font-bold mb-6">Other Options</h3>
            <div className="grid lg:grid-cols-2 gap-6">
              {otherProducts.map((product) => (
                <div key={product.id} className="relative h-full rounded-[1.25rem] border-[0.75px] border-border p-2 md:rounded-[1.5rem] md:p-3">
                  <GlowingEffect
                    spread={40}
                    glow={true}
                    disabled={false}
                    proximity={64}
                    inactiveZone={0.01}
                    borderWidth={3}
                  />
                  <div className="relative flex h-full flex-col justify-between gap-6 overflow-hidden rounded-xl border-[0.75px] bg-background p-6 shadow-sm dark:shadow-[0px_0px_27px_0px_rgba(45,45,45,0.3)]">
                    <div className="flex flex-col h-full">
                      {/* Header with Image */}
                      <div className="flex gap-4 mb-4">
                        {product.imageUrl && (
                          <div className="w-24 h-24 flex-shrink-0 bg-white rounded-md p-2 border shadow-sm flex items-center justify-center">
                            <img
                              src={product.imageUrl}
                              alt={product.name}
                              className="max-w-full max-h-full object-contain mix-blend-multiply"
                            />
                          </div>
                        )}

                        <div className="flex-1 min-w-0">
                          <h4 className="font-semibold text-lg leading-tight mb-2 line-clamp-2" title={product.name}>
                            {product.name}
                          </h4>
                          <div className="flex items-center gap-3">
                            <span className="text-xl font-bold text-primary">{product.price}</span>
                            <div className="flex items-center gap-1 bg-warning/10 px-2 py-0.5 rounded-full">
                              <Star className="w-3.5 h-3.5 fill-warning text-warning" />
                              <span className="text-sm font-medium text-warning-foreground">{product.rating}</span>
                            </div>
                          </div>
                        </div>
                      </div>

                      <div className="mb-4">
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-xs font-medium text-muted-foreground">Match Score</span>
                          <span className="text-xs font-bold">{product.score.toFixed(0)}/100</span>
                        </div>
                        <Progress value={product.score} className="h-1.5" />
                      </div>

                      <div className="grid grid-cols-2 gap-4 mb-6 flex-grow">
                        <div className="bg-success/5 p-3 rounded-md">
                          <div className="flex items-center gap-1.5 mb-2">
                            <ThumbsUp className="w-3.5 h-3.5 text-success" />
                            <span className="text-xs font-semibold text-success uppercase">Pros</span>
                          </div>
                          <ul className="space-y-1.5">
                            {product.pros.slice(0, 3).map((pro, idx) => (
                              <li key={idx} className="text-xs text-muted-foreground flex items-start gap-1.5 leading-snug">
                                <span className="w-1 h-1 bg-success rounded-full mt-1.5 flex-shrink-0" />
                                <span className="line-clamp-2">{pro}</span>
                              </li>
                            ))}
                          </ul>
                        </div>

                        <div className="bg-destructive/5 p-3 rounded-md">
                          <div className="flex items-center gap-1.5 mb-2">
                            <ThumbsDown className="w-3.5 h-3.5 text-destructive" />
                            <span className="text-xs font-semibold text-destructive uppercase">Cons</span>
                          </div>
                          <ul className="space-y-1.5">
                            {product.cons.slice(0, 3).map((con, idx) => (
                              <li key={idx} className="text-xs text-muted-foreground flex items-start gap-1.5 leading-snug">
                                <span className="w-1 h-1 bg-destructive rounded-full mt-1.5 flex-shrink-0" />
                                <span className="line-clamp-2">{con}</span>
                              </li>
                            ))}
                          </ul>
                        </div>
                      </div>

                      <Button
                        variant="outline"
                        className="w-full hover:bg-primary hover:text-primary-foreground transition-colors group"
                        onClick={() => product.url && window.open(product.url, '_blank')}
                      >
                        <ExternalLink className="w-4 h-4 mr-2 group-hover:scale-110 transition-transform" />
                        View Details
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Analysis Summary */}
        <Card className="result-card mt-12 bg-gradient-to-r from-primary/5 to-accent/5 border-primary/20">
          <div className="text-center p-6">
            <h3 className="text-xl font-semibold mb-2">Analysis Complete</h3>
            <p className="text-muted-foreground">
              {results.products.length > 0 ? (
                <>
                  Found and analyzed the perfect product match for your query.
                  Our AI has evaluated features, price, reviews, and overall value to bring you this recommendation.
                </>
              ) : (
                "No suitable products found for your query. Try refining your search criteria."
              )}
            </p>
          </div>
        </Card>
      </div>
    </section>
  );
};

export default ResultsDisplay;
