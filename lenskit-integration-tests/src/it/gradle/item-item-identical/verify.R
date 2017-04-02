message("Loading predictions")
predictions = read.csv("predictions.csv")

message("Pivoting prediction frame")

preds.wide = reshape(predictions[c("User", "Item", "Algorithm", "Prediction")],
                     timevar="Algorithm", idvar=c("User", "Item"),
                     direction="wide")

message("Checking predictions")
mismatches = is.na(preds.wide$Prediction.Standard) == is.na(preds.wide$Prediction.Normalizing)
mismatches = mismatches == is.na(preds.wide$Standard.Userwise)
pred.range = pmin(abs(preds.wide$Prediction.Standard - preds.wide$Prediction.Normalizing),
                  abs(preds.wide$Prediction.Standard - preds.wide$Prediction.Userwise))
bad.preds = pred.range > 0.001
nbad = sum(bad.preds, na.rm=TRUE)

if (sum(!mismatches) > 0) {
    print(head(subset(preds.wide, mismatches)))
    stop("item-item didn't always match on success")
} else if (nbad > 0) {
    print(head(subset(preds.wide, bad.preds)))
    stop("item-item had ", nbad, " bad predictions")
} else {
    message("Tests passed!")
}
